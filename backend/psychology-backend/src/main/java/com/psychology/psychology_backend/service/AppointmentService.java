package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.psychology.psychology_backend.dto.AppointmentWithReviewDTO;
import com.psychology.psychology_backend.entity.Appointment;

public interface AppointmentService extends IService<Appointment> {
    boolean createAppointment(Appointment appointment);
    IPage<AppointmentWithReviewDTO> getMyAppointmentsWithReview(Long userId, int page, int size);
}