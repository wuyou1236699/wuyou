package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.entity.Schedule;
import com.psychology.psychology_backend.service.AppointmentService;
import com.psychology.psychology_backend.service.CounselorService;
import com.psychology.psychology_backend.service.ReviewService;
import com.psychology.psychology_backend.service.ScheduleService;
import com.psychology.psychology_backend.utils.JwtUtil;
import com.psychology.psychology_backend.utils.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counselor")
public class CounselorController {

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AppointmentService appointmentService;

    

    /**
     * 咨询师登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Counselor counselor) {
        QueryWrapper<Counselor> wrapper = new QueryWrapper<>();
        wrapper.eq("username", counselor.getUsername());
        Counselor dbCounselor = counselorService.getOne(wrapper);

        if (dbCounselor == null) {
            return Result.error("用户名或密码错误");
        }

        // 验证密码：BCrypt优先，明文兼容（自动升级）
        String storedPwd = dbCounselor.getPassword();
        boolean matched;
        if (storedPwd != null && (storedPwd.startsWith("$2a$") || storedPwd.startsWith("$2b$"))) {
            matched = passwordUtil.matches(counselor.getPassword(), storedPwd);
        } else {
            matched = counselor.getPassword().equals(storedPwd);
            if (matched) {
                dbCounselor.setPassword(passwordUtil.encode(counselor.getPassword()));
                counselorService.updateById(dbCounselor);
            }
        }
        if (!matched) {
            return Result.error("用户名或密码错误");
        }

        if (dbCounselor.getStatus() != null && dbCounselor.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtil.generateToken(dbCounselor.getId(), "counselor");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("counselor", dbCounselor);

        return Result.success(data);
    }

    /**
     * 咨询师列表（分页），返回 avgRating 和 appointmentCount
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        Page<Counselor> pageParam = new Page<>(page, size);
        IPage<Counselor> pageResult = counselorService.page(pageParam);

        // 批量查所有咨询师的评分和预约数，避免N+1
        List<Counselor> counselors = pageResult.getRecords();
        List<Long> counselorIds = new ArrayList<>();
        for (Counselor c : counselors) {
            counselorIds.add(c.getId());
        }

        // 批量查未来排班（一次性查出所有有排班的咨询师ID）
        java.util.Set<Long> scheduledIds = new java.util.HashSet<>();
        try {
            QueryWrapper<Schedule> scheduleWrapper = new QueryWrapper<>();
            scheduleWrapper.in("counselor_id", counselorIds)
                    .ge("date", LocalDate.now())
                    .eq("is_available", 1);
            List<Schedule> upcomingSchedules = scheduleService.list(scheduleWrapper);
            for (Schedule s : upcomingSchedules) {
                scheduledIds.add(s.getCounselorId());
            }
        } catch (Exception e) {
            // 排班表可能不存在，忽略
        }

        List<Map<String, Object>> enrichedList = new ArrayList<>();
        for (Counselor c : counselors) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            item.put("gender", c.getGender());
            item.put("qualification", c.getQualification());
            item.put("title", c.getQualification());
            item.put("expertise", c.getExpertise());
            item.put("profile", c.getProfile());
            item.put("avatar", c.getAvatar());
            item.put("status", c.getStatus());
            item.put("isOnline", CounselorApiController.isOnline(c.getId()));
            item.put("hasSchedule", scheduledIds.contains(c.getId()));

            // 评分和预约数：捕获异常避免单个咨询师失败导致整个接口500
            try {
                Double avgRating = reviewService.getAverageRating(c.getId());
                item.put("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
            } catch (Exception e) {
                item.put("avgRating", 0.0);
            }
            try {
                long appointmentCount = appointmentService.lambdaQuery()
                        .eq(Appointment::getCounselorId, c.getId())
                        .ne(Appointment::getStatus, 3).count();
                item.put("appointmentCount", (int) appointmentCount);
            } catch (Exception e) {
                item.put("appointmentCount", 0);
            }

            enrichedList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", enrichedList);
        result.put("total", pageResult.getTotal());
        result.put("current", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());

        return Result.success(result);
    }

    /**
     * 咨询师详情（含评价数、预约数、评分）
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Integer id) {
        Counselor counselor = counselorService.getById(id);
        if (counselor == null) {
            return Result.error("咨询师不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", counselor.getId());
        data.put("name", counselor.getName());
        data.put("gender", counselor.getGender());
        data.put("qualification", counselor.getQualification());
        data.put("title", counselor.getQualification());
        data.put("expertise", counselor.getExpertise());
        data.put("profile", counselor.getProfile());
        data.put("avatar", counselor.getAvatar());
        data.put("status", counselor.getStatus());
        data.put("isOnline", CounselorApiController.isOnline(counselor.getId()));

        try {
            Double avgRating = reviewService.getAverageRating(counselor.getId());
            data.put("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        } catch (Exception e) {
            data.put("avgRating", 0.0);
        }
        try {
            long reviewCount = reviewService.lambdaQuery()
                    .eq(com.psychology.psychology_backend.entity.Review::getCounselorId, counselor.getId()).count();
            data.put("reviewCount", (int) reviewCount);
        } catch (Exception e) {
            data.put("reviewCount", 0);
        }
        try {
            long appointmentCount = appointmentService.lambdaQuery()
                    .eq(Appointment::getCounselorId, counselor.getId())
                    .ne(Appointment::getStatus, 3).count();
            data.put("appointmentCount", (int) appointmentCount);
        } catch (Exception e) {
            data.put("appointmentCount", 0);
        }

        return Result.success(data);
    }

    /**
     * 查询某咨询师在日期范围内的排班
     */
    @GetMapping("/schedule")
    public Result<List<Map<String, Object>>> getSchedule(@RequestParam Integer counselorId,
                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
        if (counselorId != null && counselorId != 0) {
            wrapper.eq("counselor_id", counselorId);
        }
        wrapper.ge("date", startDate)
                .le("date", endDate)
                .orderByAsc("date")
                .orderByAsc("start_time");
        List<Schedule> schedules = scheduleService.list(wrapper);
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Schedule s : schedules) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("counselorId", s.getCounselorId());
            map.put("date", s.getDate() != null ? s.getDate().toString() : "");
            map.put("startTime", s.getStartTime() != null ? s.getStartTime().toString() : "");
            map.put("endTime", s.getEndTime() != null ? s.getEndTime().toString() : "");
            map.put("status", s.getStatus());
            result.add(map);
        }
        
        return Result.success(result);
    }

    /**
     * 咨询师更新自己的资料（手机、邮箱等）
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestAttribute(value = "userId", required = false) Long counselorId,
                                        @RequestBody Map<String, String> body) {
        if (counselorId == null) return Result.error("请先登录");
        Counselor counselor = counselorService.getById(counselorId);
        if (counselor == null) return Result.error("咨询师不存在");
        if (body.containsKey("phone")) counselor.setPhone(body.get("phone"));
        if (body.containsKey("email")) counselor.setEmail(body.get("email"));
        if (body.containsKey("avatar")) counselor.setAvatar(body.get("avatar"));
        counselorService.updateById(counselor);
        return Result.success("更新成功");
    }
}