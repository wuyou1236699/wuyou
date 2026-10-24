package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.psychology.psychology_backend.entity.ChatMessage;
import com.psychology.psychology_backend.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    public List<Map<String, Object>> getChatList(Long counselorId) {
        return chatMessageMapper.getChatList(counselorId);
    }

    public List<Map<String, Object>> getUserChatList(Long userId) {
        return chatMessageMapper.getUserChatList(userId);
    }

    public List<ChatMessage> getMessages(Long userId, Long counselorId, Long appointmentId,
                                          String userType, int page, int size) {
        String deletedBy = "user".equals(userType) ? "deleted_by_user" : "deleted_by_counselor";
        int offset = (page - 1) * size;
        return chatMessageMapper.getMessagesPage(userId, counselorId, appointmentId, deletedBy, offset, size);
    }

    public int countMessages(Long userId, Long counselorId, Long appointmentId, String userType) {
        String deletedBy = "user".equals(userType) ? "deleted_by_user" : "deleted_by_counselor";
        return chatMessageMapper.countMessages(userId, counselorId, appointmentId, deletedBy);
    }

    public ChatMessage sendMessage(Long userId, Long counselorId, Long appointmentId, String sender, String content) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setCounselorId(counselorId);
        message.setAppointmentId(appointmentId != null && appointmentId > 0 ? appointmentId : null);
        message.setSender(sender);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        message.setDeletedByUser(0);
        message.setDeletedByCounselor(0);
        chatMessageMapper.insert(message);
        return message;
    }

    public void markAsRead(Long userId, Long counselorId, String readerType) {
        UpdateWrapper<ChatMessage> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("counselor_id", counselorId)
               .eq("is_read", 0);
        // readerType: "user" 标记咨询师发的消息为已读, "counselor" 标记用户发的消息为已读
        if ("user".equals(readerType)) {
            wrapper.eq("sender", "counselor");
        } else {
            wrapper.eq("sender", "user");
        }
        wrapper.set("is_read", 1);
        chatMessageMapper.update(null, wrapper);
    }

    public void deleteMessage(Long messageId, String userType) {
        UpdateWrapper<ChatMessage> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", messageId);
        if ("user".equals(userType)) {
            wrapper.set("deleted_by_user", 1);
        } else {
            wrapper.set("deleted_by_counselor", 1);
        }
        chatMessageMapper.update(null, wrapper);
    }
}
