package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.psychology.psychology_backend.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    
    List<Schedule> findAll(int page, int size);
    
    long count();
    
    Schedule findById(Long id);
    
    Schedule create(Schedule schedule);
    
    Schedule update(Long id, Schedule schedule);
    
    void delete(Long id);
    
    List<Schedule> findByCounselorId(Long counselorId);
    
    boolean addSchedule(Schedule schedule);
    
    boolean saveBatch(List<Schedule> schedules);
    
    boolean updateSchedule(Schedule schedule);
    
    boolean deleteSchedule(Long id);
    
    List<Schedule> getAvailableSchedules(Long counselorId, LocalDate start, LocalDate end);
    
    List<Schedule> list(QueryWrapper<Schedule> wrapper);
    
    IPage<Schedule> page(Page<Schedule> page, QueryWrapper<Schedule> wrapper);
    
    Schedule getById(Long id);
}