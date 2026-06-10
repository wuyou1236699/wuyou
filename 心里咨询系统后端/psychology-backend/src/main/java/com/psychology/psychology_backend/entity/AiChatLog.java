package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_chat_log")
public class AiChatLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String role;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
