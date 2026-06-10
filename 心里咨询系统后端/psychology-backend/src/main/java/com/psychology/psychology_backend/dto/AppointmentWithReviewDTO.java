package com.psychology.psychology_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentWithReviewDTO {
    private Long id;
    private Long userId;
    private Long counselorId;
    private Integer serviceType;
    private LocalDateTime appointmentTime;
    private String problem;
    private Integer status;
    private LocalDateTime createTime;
    private Boolean reviewed;
    private String counselorName;
}