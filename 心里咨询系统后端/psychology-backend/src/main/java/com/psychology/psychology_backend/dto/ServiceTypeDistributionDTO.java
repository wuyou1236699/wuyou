package com.psychology.psychology_backend.dto;

import lombok.Data;

@Data
public class ServiceTypeDistributionDTO {
    private Integer serviceType;   // 1电话 2网络 3门诊
    private Long count;
}