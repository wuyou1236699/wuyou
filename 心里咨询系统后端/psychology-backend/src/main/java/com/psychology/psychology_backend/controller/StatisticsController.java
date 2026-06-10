package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.dto.*;
import com.psychology.psychology_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/trend")
    public Result<List<AppointmentTrendDTO>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statisticsService.getAppointmentTrend(days));
    }

    @GetMapping("/counselor-ranking")
    public Result<List<CounselorWorkloadDTO>> counselorRanking() {
        return Result.success(statisticsService.getCounselorWorkloadRanking());
    }

    @GetMapping("/service-distribution")
    public Result<List<ServiceTypeDistributionDTO>> serviceDistribution() {
        return Result.success(statisticsService.getServiceTypeDistribution());
    }

    @GetMapping("/condition-distribution")
    public Result<List<ConditionDistributionDTO>> conditionDistribution() {
        return Result.success(statisticsService.getConditionDistribution());
    }
}