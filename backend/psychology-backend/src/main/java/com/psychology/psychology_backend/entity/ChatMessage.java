package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long counselorId;

    private Long appointmentId;

    private String sender;

    private String content;

    private LocalDateTime createTime;

    private Integer deletedByUser;

    private Integer deletedByCounselor;

    private Integer isRead;
}
