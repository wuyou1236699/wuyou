package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.psychology_backend.entity.Announcement;
import com.psychology.psychology_backend.mapper.AnnouncementMapper;
import com.psychology.psychology_backend.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    
    @Autowired
    private AnnouncementMapper announcementMapper;
    
    @Override
    public List<Announcement> findAll(int page, int size) {
        int offset = (page - 1) * size;
        return announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreateTime)
                .last("LIMIT " + offset + ", " + size)
        );
    }
    
    @Override
    public long count() {
        return announcementMapper.selectCount(null);
    }
    
    @Override
    public Announcement findById(Long id) {
        return announcementMapper.selectById(id);
    }
    
    @Override
    public Announcement create(Announcement announcement) {
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
        return announcement;
    }
    
    @Override
    public Announcement update(Long id, Announcement announcement) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing != null) {
            existing.setTitle(announcement.getTitle());
            existing.setContent(announcement.getContent());
            existing.setStatus(announcement.getStatus());
            existing.setPushed(announcement.getPushed());
            existing.setUpdateTime(LocalDateTime.now());
            announcementMapper.updateById(existing);
            return existing;
        }
        return null;
    }
    
    @Override
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }
}