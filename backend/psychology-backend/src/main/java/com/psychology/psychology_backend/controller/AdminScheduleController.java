package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.entity.Schedule;
import com.psychology.psychology_backend.service.CounselorService;
import com.psychology.psychology_backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/schedules")
public class AdminScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CounselorService counselorService;

    @GetMapping
    public Result<Map<String, Object>> getSchedules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) String date) {
        
        try {
            int offset = (page - 1) * size;
            QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
            
            if (counselorId != null) {
                wrapper.eq("counselor_id", counselorId);
            }
            if (date != null && !date.isEmpty()) {
                wrapper.eq("date", date);
            }

            wrapper.orderByDesc("date");
            
            List<Schedule> allSchedules = scheduleService.list(wrapper);
            long total = allSchedules.size();
            
            int start = offset;
            int end = Math.min(offset + size, allSchedules.size());
            List<Schedule> pageSchedules = start < end ? allSchedules.subList(start, end) : java.util.Collections.emptyList();
            
            List<Counselor> counselors = counselorService.list();
            Map<Long, String> counselorNameMap = new HashMap<>();
            for (Counselor c : counselors) {
                counselorNameMap.put(c.getId(), c.getName());
            }
            
            List<Map<String, Object>> records = new java.util.ArrayList<>();
            for (Schedule schedule : pageSchedules) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", schedule.getId());
                map.put("counselorId", schedule.getCounselorId());
                map.put("counselorName", counselorNameMap.getOrDefault(schedule.getCounselorId(), "未知"));
                map.put("date", schedule.getDate() != null ? schedule.getDate().toString() : "");
                map.put("startTime", schedule.getStartTime() != null ? schedule.getStartTime().toString() : "");
                map.put("endTime", schedule.getEndTime() != null ? schedule.getEndTime().toString() : "");
                String status = schedule.getStatus();
                if (status == null || status.isEmpty()) {
                    status = schedule.getIsAvailable() != null && schedule.getIsAvailable() == 1 ? "available" : "unavailable";
                }
                map.put("status", status);
                records.add(map);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", total);
            result.put("current", page);
            result.put("size", size);
            result.put("pages", (int) Math.ceil((double) total / size));
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getScheduleById(@PathVariable Long id) {
        Schedule schedule = scheduleService.getById(id);
        if (schedule == null) {
            return Result.error("排班不存在");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", schedule.getId());
        map.put("counselorId", schedule.getCounselorId());
        
        Counselor counselor = counselorService.getById(schedule.getCounselorId());
        map.put("counselorName", counselor != null ? counselor.getName() : "未知");
        map.put("date", schedule.getDate() != null ? schedule.getDate().toString() : "");
        map.put("startTime", schedule.getStartTime() != null ? schedule.getStartTime().toString() : "");
        map.put("endTime", schedule.getEndTime() != null ? schedule.getEndTime().toString() : "");
        String status = schedule.getStatus();
        if (status == null || status.isEmpty()) {
            status = schedule.getIsAvailable() != null && schedule.getIsAvailable() == 1 ? "available" : "unavailable";
        }
        map.put("status", status);
        
        return Result.success(map);
    }

    @PostMapping
    public Result<String> addSchedules(@RequestBody List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return Result.error("排班数据为空");
        }
        if (scheduleService.saveBatch(schedules)) {
            return Result.success("排班保存成功");
        }
        return Result.error("保存失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        schedule.setId(id);
        if (scheduleService.updateSchedule(schedule)) {
            return Result.success("排班更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteSchedule(@PathVariable Long id) {
        if (scheduleService.deleteSchedule(id)) {
            return Result.success("排班删除成功");
        }
        return Result.error("删除失败");
    }

    @GetMapping("/list")
    public Result<List<Schedule>> listSchedules(@RequestParam Long counselorId,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<Schedule> schedules = scheduleService.getAvailableSchedules(counselorId, start, end);
        return Result.success(schedules);
    }
}