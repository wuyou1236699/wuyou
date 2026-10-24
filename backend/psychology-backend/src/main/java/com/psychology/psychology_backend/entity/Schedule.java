package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@TableName("schedule")
public class Schedule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long counselorId;
    
    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    @TableField(exist = false)
    private Integer maxAppointments;

    @TableField(exist = false)
    private Integer currentAppointments;

    @TableField(exist = false)
    private String status;

    @TableField("is_available")
    private Integer isAvailable;
}