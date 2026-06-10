package com.psychology.psychology_backend.dto;

import lombok.Data;

@Data
public class CounselorWorkloadDTO {
    private Long counselorId;
    private String counselorName;
    private Long appointmentCount;   // 预约总量
    private Long completedCount;     // 已完成数量
}