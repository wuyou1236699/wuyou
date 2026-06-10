package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.ConsultationRecord;
import com.psychology.psychology_backend.entity.User;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.service.AppointmentService;
import com.psychology.psychology_backend.service.ConsultationRecordService;
import com.psychology.psychology_backend.service.UserService;
import com.psychology.psychology_backend.service.CounselorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentApiController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private ConsultationRecordService consultationRecordService;

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getAppointmentById(@PathVariable Long id) {
        Appointment appt = appointmentService.getById(id);
        if (appt == null) {
            return Result.error("预约不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", appt.getId());
        result.put("userId", appt.getUserId());
        result.put("counselorId", appt.getCounselorId());
        result.put("serviceType", appt.getServiceType());
        result.put("appointmentTime", appt.getAppointmentTime());
        result.put("problem", appt.getProblem());
        result.put("status", appt.getStatus());
        result.put("createTime", appt.getCreateTime());

        User user = userService.getById(appt.getUserId());
        result.put("userName", user != null ? user.getNickname() : "未知用户");

        Counselor counselor = counselorService.getById(appt.getCounselorId());
        result.put("counselorName", counselor != null ? counselor.getName() : "未知咨询师");

        return Result.success(result);
    }

    @GetMapping("/upcoming")
    public Result<List<Map<String, Object>>> getUpcomingAppointments(
            @RequestAttribute(value = "userId", required = false) Long counselorId) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<Appointment>()
            .eq("status", 0);
        if (counselorId != null) {
            wrapper.eq("counselor_id", counselorId);
        }
        wrapper.orderByAsc("appointment_time").last("LIMIT 10");
        List<Appointment> appointments = appointmentService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment appt : appointments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", appt.getId());
            item.put("userId", appt.getUserId());
            item.put("counselorId", appt.getCounselorId());

            User user = userService.getById(appt.getUserId());
            item.put("userName", user != null ? user.getNickname() : "用户");

            Counselor counselor = counselorService.getById(appt.getCounselorId());
            item.put("counselorName", counselor != null ? counselor.getName() : "咨询师");

            item.put("serviceType", appt.getServiceType());
            item.put("appointmentTime", appt.getAppointmentTime());
            item.put("problem", appt.getProblem());
            item.put("status", appt.getStatus());

            String[] serviceTypes = {"", "电话咨询", "网络咨询", "门诊咨询"};
            item.put("serviceTypeText", appt.getServiceType() != null && appt.getServiceType() >= 1 && appt.getServiceType() <= 3
                    ? serviceTypes[appt.getServiceType()] : "未知");

            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/confirmed")
    public Result<List<Map<String, Object>>> getConfirmedAppointments(
            @RequestAttribute(value = "userId", required = false) Long counselorId) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<Appointment>()
            .eq("status", 1);
        if (counselorId != null) {
            wrapper.eq("counselor_id", counselorId);
        }
        wrapper.orderByAsc("appointment_time").last("LIMIT 20");
        List<Appointment> appointments = appointmentService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment appt : appointments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", appt.getId());
            item.put("userId", appt.getUserId());
            item.put("counselorId", appt.getCounselorId());

            User user = userService.getById(appt.getUserId());
            item.put("userName", user != null ? user.getNickname() : "用户");

            Counselor counselor = counselorService.getById(appt.getCounselorId());
            item.put("counselorName", counselor != null ? counselor.getName() : "咨询师");

            item.put("serviceType", appt.getServiceType());
            item.put("appointmentTime", appt.getAppointmentTime());
            item.put("problem", appt.getProblem());
            item.put("status", appt.getStatus());

            String[] serviceTypes = {"", "电话咨询", "网络咨询", "门诊咨询"};
            item.put("serviceTypeText", appt.getServiceType() != null && appt.getServiceType() >= 1 && appt.getServiceType() <= 3
                    ? serviceTypes[appt.getServiceType()] : "未知");

            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistoryAppointments(
            @RequestAttribute(value = "userId", required = false) Long authCounselorId,
            @RequestParam(required = false) Long counselorId) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.in("status", 2, 3);
        if (authCounselorId != null) {
            wrapper.eq("counselor_id", authCounselorId);
        } else if (counselorId != null) {
            wrapper.eq("counselor_id", counselorId);
        }
        wrapper.orderByDesc("appointment_time");
        wrapper.last("LIMIT 50");

        List<Appointment> appointments = appointmentService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment appt : appointments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", appt.getId());
            item.put("userId", appt.getUserId());
            item.put("counselorId", appt.getCounselorId());

            User user = userService.getById(appt.getUserId());
            item.put("userName", user != null ? user.getNickname() : "用户");

            item.put("serviceType", appt.getServiceType());
            item.put("appointmentTime", appt.getAppointmentTime());
            item.put("status", appt.getStatus());

            String[] serviceTypes = {"", "电话咨询", "网络咨询", "门诊咨询"};
            item.put("serviceTypeText", appt.getServiceType() != null && appt.getServiceType() >= 1 && appt.getServiceType() <= 3
                    ? serviceTypes[appt.getServiceType()] : "未知");

            result.add(item);
        }

        return Result.success(result);
    }

    @PutMapping("/{id}/start")
    public Result<String> startAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        // 开始咨询不改变预约状态，保持当前状态
        appointmentService.updateById(appointment);
        return Result.success("已开始咨询");
    }

    @PutMapping("/{id}/confirm")
    public Result<String> confirmAppointment(@PathVariable Long id) {
        return Result.error("预约确认需由管理员操作，请联系平台管理员");
    }

    @PutMapping("/{id}/complete")
    public Result<String> completeAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        appointment.setStatus(2);
        appointmentService.updateById(appointment);

        // 自动创建咨询记录草稿
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getAppointmentId, id);
        if (consultationRecordService.count(wrapper) == 0) {
            ConsultationRecord record = new ConsultationRecord();
            record.setAppointmentId(id);
            record.setUserId(appointment.getUserId());
            record.setCounselorId(appointment.getCounselorId());
            record.setProblem(appointment.getProblem());
            record.setStatus(0);
            record.setCreateTime(java.time.LocalDateTime.now());
            record.setUpdateTime(java.time.LocalDateTime.now());
            consultationRecordService.save(record);
        }

        return Result.success("咨询已完成");
    }

    @PutMapping("/{id}/cancel")
    public Result<String> cancelAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        appointment.setStatus(3);
        appointmentService.updateById(appointment);
        return Result.success("预约已取消");
    }
}