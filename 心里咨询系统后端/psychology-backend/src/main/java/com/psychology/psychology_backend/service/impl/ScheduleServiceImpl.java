package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.psychology.psychology_backend.entity.Schedule;
import com.psychology.psychology_backend.mapper.ScheduleMapper;
import com.psychology.psychology_backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    
    @Autowired
    private ScheduleMapper scheduleMapper;
    
    @Override
    public List<Schedule> findAll(int page, int size) {
        int offset = (page - 1) * size;
        return scheduleMapper.selectList(
            new QueryWrapper<Schedule>()
                .orderByDesc("date")
                .last("LIMIT " + offset + ", " + size)
        );
    }
    
    @Override
    public long count() {
        return scheduleMapper.selectCount(null);
    }
    
    @Override
    public Schedule findById(Long id) {
        return scheduleMapper.selectById(id);
    }
    
    @Override
    public Schedule create(Schedule schedule) {
        if (schedule.getCurrentAppointments() == null) {
            schedule.setCurrentAppointments(0);
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus("available");
        }
        scheduleMapper.insert(schedule);
        return schedule;
    }
    
    @Override
    public Schedule update(Long id, Schedule schedule) {
        Schedule existing = scheduleMapper.selectById(id);
        if (existing != null) {
            existing.setDate(schedule.getDate());
            existing.setStartTime(schedule.getStartTime());
            existing.setEndTime(schedule.getEndTime());
            existing.setMaxAppointments(schedule.getMaxAppointments());
            existing.setStatus(schedule.getStatus());
            scheduleMapper.updateById(existing);
            return existing;
        }
        return null;
    }
    
    @Override
    public void delete(Long id) {
        scheduleMapper.deleteById(id);
    }
    
    @Override
    public List<Schedule> findByCounselorId(Long counselorId) {
        return scheduleMapper.selectList(
            new QueryWrapper<Schedule>()
                .eq("counselor_id", counselorId)
                .orderByAsc("date")
        );
    }
    
    @Override
    public boolean addSchedule(Schedule schedule) {
        if (schedule.getCurrentAppointments() == null) {
            schedule.setCurrentAppointments(0);
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus("available");
        }
        return scheduleMapper.insert(schedule) > 0;
    }
    
    @Override
    public boolean saveBatch(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }
        int count = 0;
        for (Schedule s : schedules) {
            if (s.getCurrentAppointments() == null) {
                s.setCurrentAppointments(0);
            }
            if (s.getStatus() == null) {
                s.setStatus("available");
            }
            count += scheduleMapper.insert(s);
        }
        return count == schedules.size();
    }
    
    @Override
    public boolean updateSchedule(Schedule schedule) {
        return scheduleMapper.updateById(schedule) > 0;
    }
    
    @Override
    public boolean deleteSchedule(Long id) {
        return scheduleMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<Schedule> getAvailableSchedules(Long counselorId, LocalDate start, LocalDate end) {
        return scheduleMapper.selectList(
            new QueryWrapper<Schedule>()
                .eq("counselor_id", counselorId)
                .ge("date", start)
                .le("date", end)
                .eq("is_available", 1)
                .orderByAsc("date")
                .orderByAsc("start_time")
        );
    }
    
    @Override
    public List<Schedule> list(QueryWrapper<Schedule> wrapper) {
        return scheduleMapper.selectList(wrapper);
    }
    
    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<Schedule> page(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Schedule> page, 
            QueryWrapper<Schedule> wrapper) {
        return scheduleMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Schedule getById(Long id) {
        return scheduleMapper.selectById(id);
    }
}