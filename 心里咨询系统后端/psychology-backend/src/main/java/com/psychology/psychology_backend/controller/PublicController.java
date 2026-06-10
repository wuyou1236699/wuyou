package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Announcement;
import com.psychology.psychology_backend.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/announcements")
    public Result<Map<String, Object>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<Announcement> all = announcementService.findAll(1, 100);
        List<Announcement> published = new java.util.ArrayList<>();
        for (Announcement a : all) {
            if (Integer.valueOf(1).equals(a.getPushed())) {
                published.add(a);
            }
        }

        int total = published.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<Announcement> pageList = start < end ? published.subList(start, end) : java.util.Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageList);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        return Result.success(result);
    }
}
