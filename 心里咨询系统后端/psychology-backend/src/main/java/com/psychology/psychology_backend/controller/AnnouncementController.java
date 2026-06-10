package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.psychology_backend.entity.Announcement;
import com.psychology.psychology_backend.mapper.AnnouncementMapper;
import com.psychology.psychology_backend.service.AnnouncementService;
import com.psychology.psychology_backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/announcements")
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @GetMapping("/pushed")
    public Result<List<Announcement>> getPushed() {
        List<Announcement> list = announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>().eq(Announcement::getPushed, 1)
                .orderByDesc(Announcement::getCreateTime));
        return Result.success(list);
    }

    @GetMapping
    public Result<Map<String, Object>> getAll(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        List<Announcement> announcements = announcementService.findAll(page, size);
        long total = announcementService.count();
        long totalPages = (long) Math.ceil((double) total / size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", announcements);
        result.put("total", total);
        result.put("totalPages", totalPages);
        result.put("current", page);
        
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        if (announcement != null) {
            return Result.success(announcement);
        }
        return Result.error("公告不存在");
    }
    
    @PostMapping
    public Result create(@RequestBody Announcement announcement) {
        Announcement created = announcementService.create(announcement);
        return Result.success(created);
    }
    
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Announcement announcement) {
        Announcement updated = announcementService.update(id, announcement);
        if (updated != null) {
            return Result.success(updated);
        }
        return Result.error("公告不存在");
    }
    
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/push")
    public Result push(@PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        if (announcement == null) return Result.error("公告不存在");
        // 先将所有已推送的公告取消推送（同一时间只能推送一条）
        List<Announcement> all = announcementService.findAll(1, 100);
        for (Announcement a : all) {
            if (Integer.valueOf(1).equals(a.getPushed()) && !a.getId().equals(id)) {
                a.setPushed(0);
                announcementService.update(a.getId(), a);
            }
        }
        announcement.setPushed(1);
        announcementService.update(id, announcement);
        return Result.success("已推送");
    }

    @PutMapping("/{id}/unpush")
    public Result unpush(@PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        if (announcement == null) return Result.error("公告不存在");
        announcement.setPushed(0);
        announcementService.update(id, announcement);
        return Result.success("已取消推送");
    }
}