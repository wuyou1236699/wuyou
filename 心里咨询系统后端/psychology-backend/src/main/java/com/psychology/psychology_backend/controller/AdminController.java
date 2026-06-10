package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.*;
import com.psychology.psychology_backend.mapper.*;
import com.psychology.psychology_backend.service.AdminService;
import com.psychology.psychology_backend.utils.PasswordUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CounselorMapper counselorMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ConsultationRecordMapper consultationRecordMapper;

    @Autowired
    private CounselorOnlineStatusMapper counselorOnlineStatusMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest req,
                                HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String token = adminService.adminLogin(req.username, req.password, ip);
        return Result.success(token);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        long totalUsers = userMapper.selectCount(null);
        long totalCounselors = counselorMapper.selectCount(null);
        long totalAppointments = appointmentMapper.selectCount(null);
        long totalReviews = reviewMapper.selectCount(null);

        LambdaQueryWrapper<Appointment> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.apply("DATE(appointment_time) = CURDATE()");
        long todayAppointments = appointmentMapper.selectCount(todayWrapper);

        LambdaQueryWrapper<Appointment> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Appointment::getStatus, 0);
        long pendingAppointments = appointmentMapper.selectCount(pendingWrapper);

        List<Review> reviews = reviewMapper.selectList(null);
        double averageRating = reviews.isEmpty() ? 0 :
            reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalCounselors", totalCounselors);
        stats.put("totalAppointments", totalAppointments);
        stats.put("totalReviews", totalReviews);
        stats.put("todayAppointments", todayAppointments);
        stats.put("pendingAppointments", pendingAppointments);

        // 各状态预约数
        LambdaQueryWrapper<Appointment> confirmedWrapper = new LambdaQueryWrapper<>();
        confirmedWrapper.eq(Appointment::getStatus, 1);
        stats.put("confirmedAppointments", appointmentMapper.selectCount(confirmedWrapper));

        LambdaQueryWrapper<Appointment> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Appointment::getStatus, 2);
        stats.put("completedAppointments", appointmentMapper.selectCount(completedWrapper));

        LambdaQueryWrapper<Appointment> cancelledWrapper = new LambdaQueryWrapper<>();
        cancelledWrapper.eq(Appointment::getStatus, 3);
        stats.put("cancelledAppointments", appointmentMapper.selectCount(cancelledWrapper));

        stats.put("activeUsers", totalUsers);
        stats.put("averageRating", Math.round(averageRating * 10) / 10.0);
        long onlineCounselors = counselorOnlineStatusMapper.selectCount(
            new LambdaQueryWrapper<CounselorOnlineStatus>().eq(CounselorOnlineStatus::getIsOnline, true));
        stats.put("onlineCounselors", onlineCounselors);
        stats.put("avgRating", Math.round(averageRating * 10) / 10.0);
        double satisfaction = reviews.isEmpty() ? 0 : (averageRating / 5 * 100);
        stats.put("satisfaction", Math.round(satisfaction) + "%");
        return Result.success(stats);
    }

    @GetMapping("/events")
    public Result<List<Map<String, Object>>> getEvents(@RequestParam(defaultValue = "10") int limit) {
        List<Appointment> appointments = appointmentMapper.selectList(
            new LambdaQueryWrapper<Appointment>()
                .orderByDesc(Appointment::getCreateTime)
                .last("LIMIT " + limit)
        );

        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>()
                .orderByDesc(Review::getCreateTime)
                .last("LIMIT " + limit)
        );

        List<Map<String, Object>> events = new ArrayList<>();

        for (Appointment appt : appointments) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", appt.getId());
            event.put("type", "appointment");
            event.put("title", "预约申请");
            event.put("content", "用户ID " + appt.getUserId() + " 预约了咨询师ID " + appt.getCounselorId());
            event.put("time", appt.getCreateTime() != null ? appt.getCreateTime().toString() : LocalDateTime.now().toString());
            event.put("read", false);
            events.add(event);
        }

        for (Review review : reviews) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", review.getId() + 1000);
            event.put("type", "review");
            event.put("title", "新评价");
            event.put("content", "用户ID " + review.getUserId() + " 对咨询师ID " + review.getCounselorId() + " 进行了评价");
            event.put("time", review.getCreateTime() != null ? review.getCreateTime().toString() : LocalDateTime.now().toString());
            event.put("read", false);
            events.add(event);
        }

        events.sort((a, b) -> {
            String timeA = (String) a.get("time");
            String timeB = (String) b.get("time");
            return timeB.compareTo(timeA);
        });

        if (events.size() > limit) {
            events = events.subList(0, limit);
        }

        return Result.success(events);
    }

    @GetMapping("/events/{id}")
    public Result getEventById(@PathVariable Long id) {
        // 列表中对 review 的 ID 做了 +1000 处理，这里还原
        if (id > 1000) {
            Long reviewId = id - 1000;
            Review review = reviewMapper.selectById(reviewId);
            if (review != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", id);
                event.put("type", "review");
                event.put("description", "用户" + review.getUserId() + "评价咨询师" + review.getCounselorId() + "，评分: " + review.getRating() + "分");
                event.put("createTime", review.getCreateTime() != null ? review.getCreateTime().toString() : LocalDateTime.now().toString());
                return Result.success(event);
            }
        }

        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment != null) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", appointment.getId());
            event.put("type", "appointment");
            event.put("description", "用户" + appointment.getUserId() + "预约了咨询师" + appointment.getCounselorId());
            event.put("createTime", appointment.getCreateTime() != null ? appointment.getCreateTime().toString() : LocalDateTime.now().toString());
            return Result.success(event);
        }

        return Result.error("通知不存在");
    }

    @PostMapping("/events/read-all")
    public Result markAllAsRead() {
        return Result.success("已全部标记为已读");
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> getUsers(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        long total = userMapper.selectCount(wrapper);

        int offset = (page - 1) * size;
        List<User> users = userMapper.selectList(
            wrapper.orderByAsc(User::getId).last("LIMIT " + offset + ", " + size)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("records", users);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (long) Math.ceil((double) total / size));
        return Result.success(result);
    }

    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    @PostMapping("/users")
    public Result<String> addUser(@RequestBody Map<String, Object> body) {
        User user = new User();
        user.setUsername((String) body.get("username"));
        user.setPhone((String) body.get("phone"));
        user.setStatus((String) body.getOrDefault("status", "active"));
        user.setOpenid("admin_" + System.currentTimeMillis());
        String rawPassword = (String) body.get("password");
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPassword(passwordUtil.encode(rawPassword));
        } else {
            user.setPassword(passwordUtil.encode("123456"));
        }
        userMapper.insert(user);
        return Result.success("添加成功");
    }

    @PutMapping("/users/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");
        if (body.containsKey("username")) user.setUsername((String) body.get("username"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("status")) user.setStatus((String) body.get("status"));
        String rawPassword = (String) body.get("password");
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPassword(passwordUtil.encode(rawPassword));
        }
        userMapper.updateById(user);
        return Result.success("修改成功");
    }

    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        int rows = userMapper.deleteById(id);
        return rows > 0 ? Result.success("删除成功") : Result.error("用户不存在");
    }

    @GetMapping("/counselors")
    public Result<Map<String, Object>> getCounselors(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Counselor::getName, keyword).or().like(Counselor::getExpertise, keyword));
        }
        long total = counselorMapper.selectCount(wrapper);

        int offset = (page - 1) * size;
        List<Counselor> counselors = counselorMapper.selectList(
            wrapper.orderByDesc(Counselor::getId).last("LIMIT " + offset + ", " + size)
        );

        // 批量查询在线状态
        List<Long> counselorIds = counselors.stream().map(Counselor::getId).collect(Collectors.toList());
        Map<Long, Boolean> onlineStatusMap = new HashMap<>();
        if (!counselorIds.isEmpty()) {
            List<CounselorOnlineStatus> statusList = counselorOnlineStatusMapper.selectList(
                new LambdaQueryWrapper<CounselorOnlineStatus>().in(CounselorOnlineStatus::getCounselorId, counselorIds)
            );
            for (CounselorOnlineStatus s : statusList) {
                onlineStatusMap.put(s.getCounselorId(), s.getIsOnline());
            }
        }

        // 批量查询评分
        Map<Long, Double> ratingMap = new HashMap<>();
        List<Review> allReviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>().in(Review::getCounselorId, counselorIds)
        );
        Map<Long, List<Review>> reviewsByCounselor = allReviews.stream()
            .collect(Collectors.groupingBy(Review::getCounselorId));
        for (Long cid : counselorIds) {
            List<Review> cReviews = reviewsByCounselor.getOrDefault(cid, Collections.emptyList());
            double avg = cReviews.isEmpty() ? 0 :
                cReviews.stream().mapToInt(Review::getRating).average().orElse(0);
            ratingMap.put(cid, Math.round(avg * 10) / 10.0);
        }

        // 批量查询咨询次数
        Map<Long, Long> consultCountMap = new HashMap<>();
        for (Long cid : counselorIds) {
            long count = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getCounselorId, cid)
            );
            consultCountMap.put(cid, count);
        }

        // 组装返回数据
        List<Map<String, Object>> records = new ArrayList<>();
        for (Counselor c : counselors) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            item.put("phone", c.getPhone());
            item.put("expertise", c.getExpertise());
            item.put("qualification", c.getQualification());

            // 在线状态：在线=1，离线=0
            Boolean isOnline = onlineStatusMap.get(c.getId());
            item.put("status", isOnline != null && isOnline ? 1 : 0);

            // 评分和咨询次数
            item.put("rating", ratingMap.getOrDefault(c.getId(), 0.0));
            item.put("consultCount", consultCountMap.getOrDefault(c.getId(), 0L));

            records.add(item);
        }

        // 按在线状态筛选
        if (status != null && !status.isEmpty()) {
            int targetStatus = "online".equals(status) ? 1 : 0; // 不支持busy筛选
            records = records.stream()
                .filter(r -> Integer.valueOf(targetStatus).equals(r.get("status")))
                .collect(Collectors.toList());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/appointments")
    public Result<Map<String, Object>> getAppointments(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(Appointment::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            List<Long> matchingUserIds = userMapper.selectList(
                new LambdaQueryWrapper<User>().like(User::getNickname, keyword)
            ).stream().map(User::getId).collect(java.util.stream.Collectors.toList());
            List<Long> matchingCounselorIds = counselorMapper.selectList(
                new LambdaQueryWrapper<Counselor>().like(Counselor::getName, keyword)
            ).stream().map(Counselor::getId).collect(java.util.stream.Collectors.toList());

            if (matchingUserIds.isEmpty() && matchingCounselorIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", java.util.Collections.emptyList());
                result.put("total", 0L);
                result.put("page", page);
                result.put("size", size);
                result.put("pages", 0);
                return Result.success(result);
            }
            queryWrapper.and(w -> {
                if (!matchingUserIds.isEmpty()) w.in(Appointment::getUserId, matchingUserIds);
                if (!matchingCounselorIds.isEmpty()) {
                    if (!matchingUserIds.isEmpty()) w.or();
                    w.in(Appointment::getCounselorId, matchingCounselorIds);
                }
            });
        }
        long total = appointmentMapper.selectCount(queryWrapper);

        int offset = (page - 1) * size;
        List<Appointment> appointments = appointmentMapper.selectList(
            queryWrapper.orderByDesc(Appointment::getAppointmentTime)
                .last("LIMIT " + offset + ", " + size)
        );

        String[] statusLabels = {"pending", "confirmed", "completed", "cancelled"};

        List<Map<String, Object>> records = new ArrayList<>();
        for (Appointment a : appointments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", a.getId());
            item.put("userId", a.getUserId());
            item.put("counselorId", a.getCounselorId());

            User user = userMapper.selectById(a.getUserId());
            item.put("userName", user != null ? (user.getNickname() != null ? user.getNickname() : "用户" + a.getUserId()) : "用户" + a.getUserId());

            Counselor counselor = counselorMapper.selectById(a.getCounselorId());
            item.put("counselorName", counselor != null ? counselor.getName() : "咨询师" + a.getCounselorId());

            if (a.getAppointmentTime() != null) {
                item.put("date", a.getAppointmentTime().toLocalDate().toString());
                item.put("time", a.getAppointmentTime().toLocalTime().toString().substring(0, 5));
            } else {
                item.put("date", "");
                item.put("time", "");
            }
            item.put("serviceType", a.getServiceType());
            item.put("problem", a.getProblem());

            int s = a.getStatus() != null ? a.getStatus() : 0;
            item.put("status", s >= 0 && s < statusLabels.length ? statusLabels[s] : "pending");
            item.put("statusCode", s);

            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));
        return Result.success(result);
    }

    @GetMapping("/appointments/{id}")
    public Result<Map<String, Object>> getAppointmentById(@PathVariable Long id) {
        Appointment a = appointmentMapper.selectById(id);
        if (a == null) {
            return Result.error("预约不存在");
        }

        String[] statusLabels = {"pending", "confirmed", "completed", "cancelled"};
        Map<String, Object> item = new HashMap<>();
        item.put("id", a.getId());
        item.put("userId", a.getUserId());
        item.put("counselorId", a.getCounselorId());

        User user = userMapper.selectById(a.getUserId());
        item.put("userName", user != null ? (user.getNickname() != null ? user.getNickname() : "用户" + a.getUserId()) : "用户" + a.getUserId());

        Counselor counselor = counselorMapper.selectById(a.getCounselorId());
        item.put("counselorName", counselor != null ? counselor.getName() : "咨询师" + a.getCounselorId());

        if (a.getAppointmentTime() != null) {
            item.put("date", a.getAppointmentTime().toLocalDate().toString());
            item.put("time", a.getAppointmentTime().toLocalTime().toString().substring(0, 5));
        }
        int s = a.getStatus() != null ? a.getStatus() : 0;
        item.put("status", s >= 0 && s < statusLabels.length ? statusLabels[s] : "pending");
        item.put("problem", a.getProblem());
        item.put("serviceType", a.getServiceType());

        return Result.success(item);
    }

    @PutMapping("/appointments/{id}/confirm")
    public Result<String> confirmAppointment(@PathVariable Long id) {
        Appointment a = appointmentMapper.selectById(id);
        if (a == null) return Result.error("预约不存在");
        if (a.getStatus() != 0) return Result.error("只能确认待处理状态的预约");
        a.setStatus(1);
        appointmentMapper.updateById(a);
        return Result.success("确认成功");
    }

    @PutMapping("/appointments/{id}/cancel")
    public Result<String> cancelAppointment(@PathVariable Long id) {
        Appointment a = appointmentMapper.selectById(id);
        if (a == null) return Result.error("预约不存在");
        if (a.getStatus() == 3) return Result.error("该预约已被取消");
        a.setStatus(3);
        appointmentMapper.updateById(a);
        return Result.success("取消成功");
    }

    @GetMapping("/reviews")
    public Result<Map<String, Object>> getReviews(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Review::getContent, keyword);
        }
        long total = reviewMapper.selectCount(wrapper);

        int offset = (page - 1) * size;
        List<Review> reviews = reviewMapper.selectList(
            wrapper.orderByDesc(Review::getCreateTime).last("LIMIT " + offset + ", " + size)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("records", reviews);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> getRecords(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ConsultationRecord::getProblem, keyword)
                .or().like(ConsultationRecord::getDiagnosis, keyword));
        }
        long total = consultationRecordMapper.selectCount(wrapper);

        int offset = (page - 1) * size;
        List<ConsultationRecord> records = consultationRecordMapper.selectList(
            wrapper.orderByDesc(ConsultationRecord::getCreateTime)
                .last("LIMIT " + offset + ", " + size)
        );

        List<Map<String, Object>> recordList = new ArrayList<>();
        for (ConsultationRecord record : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());

            User user = userMapper.selectById(record.getUserId());
            Counselor counselor = counselorMapper.selectById(record.getCounselorId());

            map.put("userName", user != null ? user.getNickname() : "未知");
            map.put("counselorName", counselor != null ? counselor.getName() : "未知");

            Appointment appointment = appointmentMapper.selectById(record.getAppointmentId());
            map.put("date", appointment != null && appointment.getAppointmentTime() != null ?
                appointment.getAppointmentTime().toString() : "未知");
            map.put("duration", record.getDuration() != null ? record.getDuration() + "分钟" : "-");

            Review review = reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getAppointmentId, record.getAppointmentId()));
            map.put("rating", review != null ? review.getRating() : 0);
            map.put("hasReview", review != null);

            map.put("content", record.getProblem() != null ? record.getProblem() : "-");
            map.put("suggestion", record.getSuggestions() != null ? record.getSuggestions() : "-");

            recordList.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", recordList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/records/{id}")
    public Result<Map<String, Object>> getRecordById(@PathVariable Long id) {
        ConsultationRecord record = consultationRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        
        User user = userMapper.selectById(record.getUserId());
        Counselor counselor = counselorMapper.selectById(record.getCounselorId());
        
        map.put("userName", user != null ? user.getNickname() : "未知");
        map.put("counselorName", counselor != null ? counselor.getName() : "未知");
        
        Appointment appointment = appointmentMapper.selectById(record.getAppointmentId());
        map.put("date", appointment != null && appointment.getAppointmentTime() != null ? 
            appointment.getAppointmentTime().toString() : "未知");
        map.put("duration", record.getDuration() != null ? record.getDuration() + "分钟" : "-");

        Review review = reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
            .eq(Review::getUserId, record.getUserId())
            .eq(Review::getCounselorId, record.getCounselorId()));
        map.put("rating", review != null ? review.getRating() : 0);
        map.put("hasReview", review != null);

        map.put("content", record.getProblem() != null ? record.getProblem() : "-");
        map.put("suggestion", record.getSuggestions() != null ? record.getSuggestions() : "-");

        return Result.success(map);
    }

    @DeleteMapping("/records/{id}")
    public Result deleteRecord(@PathVariable Long id) {
        int rows = consultationRecordMapper.deleteById(id);
        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ===== 咨询师 CRUD =====

    @GetMapping("/counselors/{id}")
    public Result<Map<String, Object>> getCounselorById(@PathVariable Long id) {
        Counselor counselor = counselorMapper.selectById(id);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }

        // 查询评分和咨询次数
        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>().eq(Review::getCounselorId, id));
        double avgRating = reviews.isEmpty() ? 0 :
            reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        long consultCount = appointmentMapper.selectCount(
            new LambdaQueryWrapper<Appointment>().eq(Appointment::getCounselorId, id));

        Map<String, Object> data = new HashMap<>();
        data.put("id", counselor.getId());
        data.put("name", counselor.getName());
        data.put("username", counselor.getUsername());
        data.put("phone", counselor.getPhone());
        data.put("email", counselor.getEmail());
        data.put("expertise", counselor.getExpertise());
        data.put("qualification", counselor.getQualification());
        data.put("description", counselor.getProfile());
        data.put("avatar", counselor.getAvatar());
        data.put("rating", Math.round(avgRating * 10) / 10.0);
        data.put("consultCount", (int) consultCount);

        // 真实在线状态
        CounselorOnlineStatus onlineStatus = counselorOnlineStatusMapper.selectOne(
            new LambdaQueryWrapper<CounselorOnlineStatus>().eq(CounselorOnlineStatus::getCounselorId, id));
        boolean isOnline = onlineStatus != null && Boolean.TRUE.equals(onlineStatus.getIsOnline());
        data.put("status", isOnline ? "online" : "offline");
        data.put("statusCode", isOnline ? 1 : 0);

        return Result.success(data);
    }

    @PostMapping("/counselors")
    public Result<String> addCounselor(@RequestBody Map<String, Object> body) {
        Counselor counselor = new Counselor();
        counselor.setName((String) body.get("name"));
        counselor.setPhone((String) body.get("phone"));
        counselor.setExpertise((String) body.get("expertise"));
        counselor.setProfile((String) body.get("description"));

        // 找到最小可用ID，填补删除造成的空缺
        Long nextId = 1L;
        java.util.TreeSet<Long> idSet = new java.util.TreeSet<>(
            counselorMapper.selectObjs(
                new LambdaQueryWrapper<Counselor>()
                    .select(Counselor::getId)
                    .orderByAsc(Counselor::getId)
            ).stream().map(obj -> (Long) obj).toList()
        );
        for (Long id : idSet) {
            if (id.equals(nextId)) {
                nextId++;
            } else {
                break;
            }
        }
        counselor.setId(nextId);

        // 状态: online→1, offline→0, busy→2
        String statusStr = (String) body.get("status");
        if ("online".equals(statusStr)) {
            counselor.setStatus(1);
        } else if ("busy".equals(statusStr)) {
            counselor.setStatus(2);
        } else {
            counselor.setStatus(0);
        }

        // 用户名：使用手机号
        String phone = (String) body.get("phone");
        counselor.setUsername(phone != null ? phone : "");

        // 密码 BCrypt 加密
        String rawPassword = (String) body.get("password");
        if (rawPassword != null && !rawPassword.isEmpty()) {
            counselor.setPassword(passwordUtil.encode(rawPassword));
        } else {
            counselor.setPassword(passwordUtil.encode("123456"));
        }

        counselorMapper.insert(counselor);
        return Result.success("添加成功");
    }

    @PutMapping("/counselors/{id}")
    public Result<String> updateCounselor(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body) {
        Counselor counselor = counselorMapper.selectById(id);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }

        if (body.containsKey("name")) counselor.setName((String) body.get("name"));
        if (body.containsKey("phone")) counselor.setPhone((String) body.get("phone"));
        if (body.containsKey("expertise")) counselor.setExpertise((String) body.get("expertise"));
        if (body.containsKey("description")) counselor.setProfile((String) body.get("description"));

        if (body.containsKey("status")) {
            String statusStr = (String) body.get("status");
            if ("online".equals(statusStr)) {
                counselor.setStatus(1);
            } else if ("busy".equals(statusStr)) {
                counselor.setStatus(2);
            } else {
                counselor.setStatus(0);
            }
        }

        String rawPassword = (String) body.get("password");
        if (rawPassword != null && !rawPassword.isEmpty()) {
            counselor.setPassword(passwordUtil.encode(rawPassword));
        }

        counselorMapper.updateById(counselor);
        return Result.success("修改成功");
    }

    @DeleteMapping("/counselors/{id}")
    public Result<String> deleteCounselor(@PathVariable Long id) {
        int rows = counselorMapper.deleteById(id);
        if (rows > 0) {
            // 重置自增ID，避免编号跳跃
            Long maxId = counselorMapper.selectObjs(
                new LambdaQueryWrapper<Counselor>()
                    .select(Counselor::getId)
                    .orderByDesc(Counselor::getId)
                    .last("LIMIT 1")
            ).stream().findFirst().map(obj -> (Long) obj).orElse(0L);
            counselorMapper.resetAutoIncrement(maxId + 1);
            return Result.success("删除成功");
        }
        return Result.error("咨询师不存在");
    }
}