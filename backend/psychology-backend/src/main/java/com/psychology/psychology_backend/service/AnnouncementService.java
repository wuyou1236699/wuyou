package com.psychology.psychology_backend.service;

import com.psychology.psychology_backend.entity.Announcement;

import java.util.List;

public interface AnnouncementService {
    
    List<Announcement> findAll(int page, int size);
    
    long count();
    
    Announcement findById(Long id);
    
    Announcement create(Announcement announcement);
    
    Announcement update(Long id, Announcement announcement);
    
    void delete(Long id);
}