package com.psychology.psychology_backend.dto;

import lombok.Data;

@Data
public class DashboardDTO {
    private Long totalUsers;          // 总用户数
    private Long todayAppointments;   // 今日预约数
    private Long pendingAppointments; // 待确认预约数
    private Long totalCounselors;     // 咨询师总数
}