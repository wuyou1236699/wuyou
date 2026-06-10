package com.psychology.psychology_backend.dto;

import lombok.Data;

@Data
public class CounselorRecommendDTO {
    private Long id;
    private String name;
    private String expertise;
    private String profile;
    private String avatar;
    private Double avgRating;
    private Integer appointmentCount;
    private Double matchScore; // 内部使用，不必须返回前端
}