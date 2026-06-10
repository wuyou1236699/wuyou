package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.dto.AppointmentWithReviewDTO;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/my")
    public Result<IPage<AppointmentWithReviewDTO>> myAppointments(@RequestAttribute Long userId,
                                                                  @RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(appointmentService.getMyAppointmentsWithReview(userId, page, size));
    }

    @PostMapping("/create")
    public Result<String> create(@RequestBody Appointment appointment, @RequestAttribute Long userId) {
        appointment.setUserId(userId);
        if (appointmentService.createAppointment(appointment)) {
            return Result.success("预约成功");
        } else {
            return Result.error("预约失败");
        }
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id, @RequestAttribute Long userId) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getUserId().equals(userId)) {
            return Result.error("预约不存在");
        }
        // 修改这里：24 -> 6
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now().plusHours(6))) {
            return Result.error("距离预约时间不足6小时，无法取消");
        }
        if (appointment.getStatus() != 0 && appointment.getStatus() != 1) {
            return Result.error("当前状态不可取消");
        }
        appointment.setStatus(3);
        appointmentService.updateById(appointment);
        return Result.success("取消成功");
    }

    // 其他方法（如创建预约）请保留原有实现
}