package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_task")
public class AiTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userMessage;
    private Integer status; // 0处理中 1完成 2失败
    private String aiResponse;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}