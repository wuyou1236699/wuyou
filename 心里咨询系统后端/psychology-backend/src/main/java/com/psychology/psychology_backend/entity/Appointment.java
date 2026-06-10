package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long counselorId;
    private Integer serviceType; // 1电话 2网络 3门诊
    private LocalDateTime appointmentTime;
    private String problem;
    private Integer status; // 0待确认 1已确认 2已完成 3已取消
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String counselorName;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private Boolean reviewed;
}