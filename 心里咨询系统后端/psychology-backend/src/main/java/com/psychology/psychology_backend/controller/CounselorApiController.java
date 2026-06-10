package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.CounselorOnlineStatus;
import com.psychology.psychology_backend.entity.Review;
import com.psychology.psychology_backend.mapper.CounselorOnlineStatusMapper;
import com.psychology.psychology_backend.service.CounselorService;
import com.psychology.psychology_backend.service.AppointmentService;
import com.psychology.psychology_backend.service.ReviewService;
import com.psychology.psychology_backend.mapper.ReviewMapper;
import com.psychology.psychology_backend.dto.CounselorRecommendDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counselors")
public class CounselorApiController {

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private CounselorOnlineStatusMapper onlineStatusMapper;

    private static CounselorOnlineStatusMapper staticOnlineStatusMapper;

    @PostConstruct
    public void init() {
        staticOnlineStatusMapper = onlineStatusMapper;
    }

    public static boolean isOnline(Long counselorId) {
        if (staticOnlineStatusMapper == null) return false;
        QueryWrapper<CounselorOnlineStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("counselor_id", counselorId);
        CounselorOnlineStatus status = staticOnlineStatusMapper.selectOne(wrapper);
        return status != null && Boolean.TRUE.equals(status.getIsOnline());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getCounselorById(@PathVariable Long id) {
        Counselor counselor = counselorService.getById(id);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", counselor.getId());
        result.put("name", counselor.getName());
        result.put("gender", counselor.getGender());
        result.put("qualification", counselor.getQualification());
        result.put("title", counselor.getQualification());
        result.put("expertise", counselor.getExpertise());
        result.put("profile", counselor.getProfile());
        result.put("avatar", counselor.getAvatar());
        result.put("experience", counselor.getExpertise());
        result.put("phone", counselor.getPhone() != null ? counselor.getPhone() : "");
        result.put("email", counselor.getEmail() != null ? counselor.getEmail() : "");

        // 计算评分
        Double avgRating = reviewService.getAverageRating(id);
        avgRating = avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
        result.put("rating", avgRating);

        // 计算好评率（评分>=4的评价占比，或者用 avgRating/5 * 100）
        List<Review> reviews = reviewService.getReviewsByCounselor(id, 1, 1000);
        long goodReviews = reviews.stream().filter(r -> r.getRating() != null && r.getRating() >= 4).count();
        int positiveRate = reviews.isEmpty() ? 0 : (int) Math.round(goodReviews * 100.0 / reviews.size());
        result.put("positiveRate", positiveRate);

        // 累计咨询数（已完成的预约）
        long totalConsultations = appointmentService.lambdaQuery()
                .eq(Appointment::getCounselorId, id)
                .eq(Appointment::getStatus, 2)
                .count();
        result.put("totalConsultations", (int) totalConsultations);

        // 预约人数
        long appointmentCount = appointmentService.lambdaQuery()
                .eq(Appointment::getCounselorId, id)
                .count();
        result.put("appointmentCount", (int) appointmentCount);

        // 在线时长：从counselor_online_status表读取
        QueryWrapper<CounselorOnlineStatus> statusWrapper = new QueryWrapper<>();
        statusWrapper.eq("counselor_id", id);
        CounselorOnlineStatus onlineStatus = onlineStatusMapper.selectOne(statusWrapper);
        long onlineHours = 0;
        if (onlineStatus != null && Boolean.TRUE.equals(onlineStatus.getIsOnline())
                && onlineStatus.getLastOnlineTime() != null) {
            long minutes = java.time.Duration.between(onlineStatus.getLastOnlineTime(), LocalDateTime.now()).toMinutes();
            onlineHours = Math.max(0, minutes / 60);
        }
        result.put("onlineHours", (int) onlineHours);

        return Result.success(result);
    }

    @GetMapping("/online-status")
    public Result<Map<String, Object>> getOnlineStatus(@RequestAttribute(value = "userId", required = false) Long counselorId) {
        Map<String, Object> result = new HashMap<>();
        Boolean isOnline = isOnline(counselorId);
        result.put("isOnline", isOnline);
        return Result.success(result);
    }

    @GetMapping("/online-status-list")
    public Result<Map<Long, Boolean>> getAllOnlineStatus() {
        Map<Long, Boolean> result = new HashMap<>();
        List<CounselorOnlineStatus> list = onlineStatusMapper.selectList(null);
        if (list != null) {
            for (CounselorOnlineStatus status : list) {
                result.put(status.getCounselorId(), status.getIsOnline());
            }
        }
        return Result.success(result);
    }

    @GetMapping("/recommend")
    public Result<List<CounselorRecommendDTO>> recommend(
            @RequestParam(required = false) String problemType) {
        return Result.success(counselorService.recommend(problemType));
    }

    @PutMapping("/online-status")
    public Result<String> setOnlineStatus(@RequestAttribute(value = "userId", required = false) Long counselorId,
                                         @RequestBody Map<String, Boolean> body) {
        Boolean isOnline = body.get("isOnline");
        if (counselorId == null) {
            return Result.error("请先登录");
        }

        QueryWrapper<CounselorOnlineStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("counselor_id", counselorId);
        CounselorOnlineStatus status = onlineStatusMapper.selectOne(wrapper);

        if (status == null) {
            status = new CounselorOnlineStatus();
            status.setCounselorId(counselorId);
            status.setIsOnline(isOnline != null && isOnline);
            status.setLastOnlineTime(isOnline != null && isOnline ? LocalDateTime.now() : null);
            status.setLastHeartbeatTime(isOnline != null && isOnline ? LocalDateTime.now() : null);
            onlineStatusMapper.insert(status);
        } else {
            status.setIsOnline(isOnline != null && isOnline);
            if (isOnline != null && isOnline) {
                status.setLastOnlineTime(LocalDateTime.now());
                status.setLastHeartbeatTime(LocalDateTime.now());
            }
            onlineStatusMapper.updateById(status);
        }

        return Result.success(isOnline != null && isOnline ? "已上线" : "已离线");
    }

    @PutMapping("/heartbeat")
    public Result<String> heartbeat(@RequestAttribute(value = "userId", required = false) Long counselorId) {
        if (counselorId == null) return Result.error("请先登录");
        QueryWrapper<CounselorOnlineStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("counselor_id", counselorId);
        CounselorOnlineStatus status = onlineStatusMapper.selectOne(wrapper);
        if (status != null && Boolean.TRUE.equals(status.getIsOnline())) {
            status.setLastHeartbeatTime(LocalDateTime.now());
            onlineStatusMapper.updateById(status);
        }
        return Result.success("ok");
    }

    @Scheduled(fixedRate = 300000)
    public void autoOfflineTimeout() {
        QueryWrapper<CounselorOnlineStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("is_online", true);
        List<CounselorOnlineStatus> onlineList = onlineStatusMapper.selectList(wrapper);
        LocalDateTime now = LocalDateTime.now();
        for (CounselorOnlineStatus s : onlineList) {
            if (s.getLastHeartbeatTime() != null
                    && java.time.Duration.between(s.getLastHeartbeatTime(), now).toMinutes() > 15) {
                s.setIsOnline(false);
                onlineStatusMapper.updateById(s);
            }
        }
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestAttribute(value = "userId", required = false) Long counselorId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 根据review表计算咨询师的平均评分
        if (counselorId != null) {
            QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
            reviewWrapper.eq("counselor_id", counselorId);
            List<Review> reviews = reviewMapper.selectList(reviewWrapper);
            
            if (reviews != null && !reviews.isEmpty()) {
                double totalRating = 0.0;
                for (Review review : reviews) {
                    if (review.getRating() != null) {
                        totalRating += review.getRating();
                    }
                }
                double avgRating = totalRating / reviews.size();
                // 四舍五入保留一位小数
                BigDecimal bd = new BigDecimal(avgRating).setScale(1, RoundingMode.HALF_UP);
                stats.put("rating", bd.doubleValue());
            } else {
                stats.put("rating", 0.0);
            }
        } else {
            stats.put("rating", 0.0);
        }
        
        // 今日预约数
        LocalDate today = LocalDate.now();
        QueryWrapper<Appointment> todayWrapper = new QueryWrapper<>();
        if (counselorId != null) {
            todayWrapper.eq("counselor_id", counselorId);
        }
        todayWrapper.apply("DATE(appointment_time) = DATE(NOW())");
        long todayCount = appointmentService.count(todayWrapper);
        stats.put("todayAppointments", (int) todayCount);
        
        // 本月咨询数（已完成的预约）
        QueryWrapper<Appointment> monthWrapper = new QueryWrapper<>();
        if (counselorId != null) {
            monthWrapper.eq("counselor_id", counselorId);
        }
        monthWrapper.in("status", 1, 2);
        monthWrapper.apply("YEAR(appointment_time) = YEAR(NOW()) AND MONTH(appointment_time) = MONTH(NOW())");
        long monthCount = appointmentService.count(monthWrapper);
        stats.put("monthConsultations", (int) monthCount);
        
        // 待处理消息（暂时先设为0，后续可以添加消息功能）
        stats.put("pendingMessages", 0);
        
        return Result.success(stats);
    }
}
