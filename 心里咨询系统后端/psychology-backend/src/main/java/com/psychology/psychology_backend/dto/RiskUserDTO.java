package com.psychology.psychology_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskUserDTO {
    private Long userId;
    private String nickname;
    private String phone;
    private String riskLevel;   // 高风险、中风险等
    private LocalDateTime lastRiskTime;
    private String content;     // 触发风险的关键内容
}