package com.psychology.psychology_backend.service.impl;

import com.psychology.psychology_backend.dto.*;
import com.psychology.psychology_backend.mapper.AppointmentMapper;
import com.psychology.psychology_backend.mapper.CounselorMapper;
import com.psychology.psychology_backend.mapper.UserMapper;
import com.psychology.psychology_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private CounselorMapper counselorMapper;

    @Override
    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalUsers(userMapper.countTotalUsers());
        dto.setTodayAppointments(appointmentMapper.countTodayAppointments());
        dto.setPendingAppointments(appointmentMapper.countPendingAppointments());
        dto.setTotalCounselors(counselorMapper.countActiveCounselors());
        return dto;
    }

    @Override
    public List<AppointmentTrendDTO> getAppointmentTrend(int days) {
        List<Map<String, Object>> rows = appointmentMapper.getLast7DaysTrend(); // 默认7天，可扩展
        List<AppointmentTrendDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            AppointmentTrendDTO dto = new AppointmentTrendDTO();
            dto.setDate(row.get("date").toString());
            dto.setCount(((Number) row.get("count")).longValue());
            result.add(dto);
        }
        // 补全缺失的日期（例如近7天无预约的日期填0）
        return fillMissingDates(result, days);
    }

    private List<AppointmentTrendDTO> fillMissingDates(List<AppointmentTrendDTO> existing, int days) {
        List<AppointmentTrendDTO> full = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            AppointmentTrendDTO dto = new AppointmentTrendDTO();
            dto.setDate(dateStr);
            dto.setCount(0L);
            for (AppointmentTrendDTO e : existing) {
                if (e.getDate().equals(dateStr)) {
                    dto.setCount(e.getCount());
                    break;
                }
            }
            full.add(dto);
        }
        return full;
    }

    @Override
    public List<CounselorWorkloadDTO> getCounselorWorkloadRanking() {
        List<Map<String, Object>> rows = appointmentMapper.getCounselorWorkloadRanking();
        List<CounselorWorkloadDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            CounselorWorkloadDTO dto = new CounselorWorkloadDTO();
            dto.setCounselorId(((Number) row.get("counselorId")).longValue());
            dto.setCounselorName((String) row.get("counselorName"));
            dto.setAppointmentCount(((Number) row.get("appointmentCount")).longValue());
            dto.setCompletedCount(((Number) row.get("completedCount")).longValue());
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<ServiceTypeDistributionDTO> getServiceTypeDistribution() {
        List<Map<String, Object>> rows = appointmentMapper.getServiceTypeDistribution();
        List<ServiceTypeDistributionDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            ServiceTypeDistributionDTO dto = new ServiceTypeDistributionDTO();
            dto.setServiceType(((Number) row.get("serviceType")).intValue());
            dto.setCount(((Number) row.get("count")).longValue());
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<RiskUserDTO> getRiskUsers() {
        // 暂未实现，返回空列表。后续可查询 ai_chat_log 表 risk_flag=1 的用户
        return new ArrayList<>();
    }

    @Override
    public List<ConditionDistributionDTO> getConditionDistribution() {
        List<Map<String, Object>> rows = appointmentMapper.getConditionDistribution();
        List<ConditionDistributionDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            ConditionDistributionDTO dto = new ConditionDistributionDTO();
            dto.setCondition((String) row.get("condition"));
            dto.setCount(((Number) row.get("count")).longValue());
            result.add(dto);
        }
        return result;
    }
}