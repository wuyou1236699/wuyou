package com.psychology.psychology_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.psychology_backend.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    // 今日预约数
    @Select("SELECT COUNT(*) FROM appointment WHERE DATE(create_time) = CURDATE()")
    Long countTodayAppointments();

    // 待确认预约数
    @Select("SELECT COUNT(*) FROM appointment WHERE status = 0")
    Long countPendingAppointments();

    // 近7天预约趋势（按日期分组）
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM appointment " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getLast7DaysTrend();

    // 咨询师工作量排名（预约总量前5，含完成量）
    @Select("SELECT c.id as counselorId, c.name as counselorName, COUNT(a.id) as appointmentCount, " +
            "SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) as completedCount " +
            "FROM counselor c " +
            "LEFT JOIN appointment a ON c.id = a.counselor_id " +
            "GROUP BY c.id " +
            "ORDER BY appointmentCount DESC " +
            "LIMIT 5")
    List<Map<String, Object>> getCounselorWorkloadRanking();

    // 服务类型分布
    @Select("SELECT service_type as serviceType, COUNT(*) as count " +
            "FROM appointment " +
            "WHERE service_type IS NOT NULL " +
            "GROUP BY service_type")
    List<Map<String, Object>> getServiceTypeDistribution();

    // 病情分布：从 problem 字段提取【标签】，统计每个标签的预约数
    @Select("SELECT " +
            "  CASE " +
            "    WHEN problem LIKE '【%】%' THEN SUBSTRING_INDEX(SUBSTRING_INDEX(problem, '】', 1), '【', -1) " +
            "    ELSE '其他' " +
            "  END AS `condition`, " +
            "  COUNT(*) AS `count` " +
            "FROM appointment " +
            "GROUP BY `condition` " +
            "ORDER BY `count` DESC")
    List<Map<String, Object>> getConditionDistribution();
}