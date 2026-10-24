package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.entity.Schedule;
import com.psychology.psychology_backend.service.ScheduleService;
import com.psychology.psychology_backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @GetMapping
    public Result<Map<String, Object>> getAll(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        List<Schedule> schedules = scheduleService.findAll(page, size);
        long total = scheduleService.count();
        long totalPages = (long) Math.ceil((double) total / size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", schedules);
        result.put("total", total);
        result.put("totalPages", totalPages);
        result.put("current", page);
        
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Schedule schedule = scheduleService.findById(id);
        if (schedule != null) {
            return Result.success(schedule);
        }
        return Result.error("排班不存在");
    }
    
    @GetMapping("/counselor/{counselorId}")
    public Result getByCounselorId(@PathVariable Long counselorId) {
        List<Schedule> schedules = scheduleService.findByCounselorId(counselorId);
        return Result.success(schedules);
    }
    
    @PostMapping
    public Result create(@RequestBody Schedule schedule) {
        Schedule created = scheduleService.create(schedule);
        return Result.success(created);
    }
    
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Schedule schedule) {
        Schedule updated = scheduleService.update(id, schedule);
        if (updated != null) {
            return Result.success(updated);
        }
        return Result.error("排班不存在");
    }
    
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return Result.success("删除成功");
    }
}