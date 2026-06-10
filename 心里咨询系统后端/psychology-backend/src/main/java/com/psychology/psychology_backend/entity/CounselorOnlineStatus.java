package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("counselor_online_status")
public class CounselorOnlineStatus {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long counselorId;
    private Boolean isOnline;
    private java.time.LocalDateTime lastOnlineTime;
    private java.time.LocalDateTime lastHeartbeatTime;
}
