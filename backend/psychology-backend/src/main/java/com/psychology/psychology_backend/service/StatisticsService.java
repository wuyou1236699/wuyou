package com.psychology.psychology_backend.service;

import com.psychology.psychology_backend.dto.*;

import java.util.List;

public interface StatisticsService {
    DashboardDTO getDashboard();
    List<AppointmentTrendDTO> getAppointmentTrend(int days);
    List<CounselorWorkloadDTO> getCounselorWorkloadRanking();
    List<ServiceTypeDistributionDTO> getServiceTypeDistribution();
    List<RiskUserDTO> getRiskUsers();  // 暂未实现，返回空
    List<ConditionDistributionDTO> getConditionDistribution();
}