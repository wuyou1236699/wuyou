package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.dto.AppointmentWithReviewDTO;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.entity.Review;
import com.psychology.psychology_backend.entity.Schedule;
import com.psychology.psychology_backend.mapper.AppointmentMapper;
import com.psychology.psychology_backend.mapper.ReviewMapper;
import com.psychology.psychology_backend.service.AppointmentService;
import com.psychology.psychology_backend.service.CounselorService;
import com.psychology.psychology_backend.service.ScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ReviewMapper reviewMapper;  // 直接使用 Mapper，避免循环依赖

    @Override
    public boolean createAppointment(Appointment appointment) {
        // 校验咨询师
        Counselor counselor = counselorService.getById(appointment.getCounselorId());
        if (counselor == null || counselor.getStatus() != 1) {
            throw new RuntimeException("咨询师不存在或已停用");
        }

        // 校验排班
        LocalDateTime appointmentTime = appointment.getAppointmentTime();
        LocalDate date = appointmentTime.toLocalDate();
        LocalTime time = appointmentTime.toLocalTime();

        List<Schedule> schedules = scheduleService.list(new QueryWrapper<Schedule>()
                .eq("counselor_id", appointment.getCounselorId())
                .eq("date", date)
                .eq("is_available", 1));
        if (schedules.isEmpty()) {
            throw new RuntimeException("该咨询师当天无排班，请选择其他日期");
        }
        boolean valid = false;
        for (Schedule s : schedules) {
            if (!time.isBefore(s.getStartTime()) && !time.isAfter(s.getEndTime())) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new RuntimeException("预约时间不在咨询师的排班时段内，请重新选择");
        }

        // 时段冲突检测
        long count = lambdaQuery()
                .eq(Appointment::getCounselorId, appointment.getCounselorId())
                .eq(Appointment::getAppointmentTime, appointment.getAppointmentTime())
                .ne(Appointment::getStatus, 3)
                .count();
        if (count > 0) {
            throw new RuntimeException("该时段已被预约，请选择其他时间");
        }

        appointment.setStatus(0);
        return save(appointment);
    }

    @Override
    public IPage<AppointmentWithReviewDTO> getMyAppointmentsWithReview(Long userId, int page, int size) {
        Page<Appointment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getUserId, userId)
                .orderByDesc(Appointment::getCreateTime);
        IPage<Appointment> appointmentPage = this.page(pageObj, wrapper);

        List<AppointmentWithReviewDTO> dtoList = appointmentPage.getRecords().stream().map(apt -> {
            AppointmentWithReviewDTO dto = new AppointmentWithReviewDTO();
            BeanUtils.copyProperties(apt, dto);
            // 查询是否已评价（使用 reviewMapper 避免循环依赖）
            long reviewedCount = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getAppointmentId, apt.getId()));
            dto.setReviewed(reviewedCount > 0);
            // 填充咨询师姓名
            Counselor counselor = counselorService.getById(apt.getCounselorId());
            if (counselor != null) {
                dto.setCounselorName(counselor.getName());
            }
            return dto;
        }).collect(Collectors.toList());

        Page<AppointmentWithReviewDTO> resultPage = new Page<>(page, size, appointmentPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }
}