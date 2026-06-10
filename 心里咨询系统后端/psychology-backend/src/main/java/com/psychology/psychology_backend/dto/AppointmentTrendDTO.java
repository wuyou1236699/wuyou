package com.psychology.psychology_backend.dto;

import lombok.Data;

@Data
public class AppointmentTrendDTO {
    private String date;    // 日期（yyyy-MM-dd）
    private Long count;     // 预约数量
}