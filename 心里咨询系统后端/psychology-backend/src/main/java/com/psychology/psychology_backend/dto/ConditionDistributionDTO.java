package com.psychology.psychology_backend.dto;

import lombok.Data;

@Data
public class ConditionDistributionDTO {
    private String condition;
    private Long count;
}
