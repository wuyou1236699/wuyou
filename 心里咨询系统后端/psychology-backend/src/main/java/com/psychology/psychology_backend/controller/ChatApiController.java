package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.ChatMessage;
import com.psychology.psychology_backend.handler.ChatWebSocketHandler;
import com.psychology.psychology_backend.mapper.AppointmentMapper;
import com.psychology.psychology_backend.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getChatList(
            @RequestAttribute(value = "userId", required = false) Long currentUserId,
            @RequestAttribute(value = "userType", required = false) String userType) {

        if (currentUserId == null || !"counselor".equals(userType)) {
            return Result.error("请先登录");
        }

        List<Map<String, Object>> chatList = chatMessageService.getChatList(currentUserId);

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd");
        List<Map<String, Object>> result = chatList.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", row.get("userId"));
            map.put("userName", row.get("userName"));
            map.put("userAvatar", row.get("userAvatar"));
            map.put("lastMessage", row.get("lastMessage"));
            Object lastTime = row.get("lastTime");
            if (lastTime instanceof LocalDateTime) {
                map.put("lastTime", ((LocalDateTime) lastTime).format(timeFmt));
            } else {
                map.put("lastTime", "");
            }
            map.put("unreadCount", row.get("unreadCount"));

            // 预约信息
            Object apptId = row.get("appointmentId");
            boolean isDirect = apptId == null;
            map.put("isDirect", isDirect);
            map.put("appointmentId", apptId);
            Object apptTime = row.get("appointmentTime");

            // 病情标签：从 problem 字段提取【标签】
            Object problemObj = row.get("problem");
            String condition = "其他";
            if (problemObj != null && problemObj.toString().startsWith("【")) {
                int end = problemObj.toString().indexOf("】");
                if (end > 1) {
                    condition = problemObj.toString().substring(1, end);
                }
            }
            map.put("condition", isDirect ? "直接对话" : condition);
            if (apptTime instanceof LocalDateTime) {
                map.put("appointmentTime", ((LocalDateTime) apptTime).format(dateFmt));
            } else if (apptTime != null) {
                map.put("appointmentTime", apptTime.toString());
            } else {
                map.put("appointmentTime", "");
            }
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/user-list")
    public Result<List<Map<String, Object>>> getUserChatList(
            @RequestAttribute(value = "userId", required = false) Long currentUserId,
            @RequestAttribute(value = "userType", required = false) String userType) {

        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        List<Map<String, Object>> chatList = chatMessageService.getUserChatList(currentUserId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<Map<String, Object>> result = chatList.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("counselorId", row.get("counselorId"));
            map.put("counselorName", row.get("counselorName"));
            map.put("counselorAvatar", row.get("counselorAvatar"));
            map.put("appointmentId", row.get("appointmentId"));
            map.put("appointmentTime", row.get("appointmentTime"));
            map.put("lastMessage", row.get("lastMessage"));
            map.put("unreadCount", row.get("unreadCount"));
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessages(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestAttribute(value = "userId", required = false) Long currentUserId,
            @RequestAttribute(value = "userType", required = false) String userType) {

        try {
            if (currentUserId == null) {
                return Result.error("请先登录");
            }

            Long actualUserId = userId;
            Long actualCounselorId = counselorId;

            if ("user".equals(userType)) {
                actualUserId = currentUserId;
            } else {
                actualCounselorId = currentUserId;
            }

            if (actualUserId == null || actualCounselorId == null) {
                return Result.error("参数不完整");
            }

            List<ChatMessage> messageList = chatMessageService.getMessages(
                    actualUserId, actualCounselorId, appointmentId, userType, page, size);
            int total = chatMessageService.countMessages(
                    actualUserId, actualCounselorId, appointmentId, userType);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            List<Map<String, Object>> messages = messageList.stream().map(msg -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", msg.getId());
                map.put("content", msg.getContent());
                map.put("sender", msg.getSender());
                map.put("time", msg.getCreateTime() != null ? msg.getCreateTime().format(formatter) : "");
                map.put("createTime", msg.getCreateTime());
                return map;
            }).collect(Collectors.toList());

            boolean hasMore = page * size < total;

            Map<String, Object> result = new HashMap<>();
            result.put("messages", messages);
            result.put("hasMore", hasMore);
            result.put("total", total);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }

    @PostMapping("/send")
    public Result<String> sendMessage(
            @RequestBody Map<String, Object> message,
            @RequestAttribute(value = "userId", required = false) Long currentUserId,
            @RequestAttribute(value = "userType", required = false) String userType) {

        try {
            if (currentUserId == null) {
                return Result.error("请先登录");
            }

            Object userIdObj = message.get("userId");
            Object counselorIdObj = message.getOrDefault("counselorId", 0);
            Object appointmentIdObj = message.get("appointmentId");
            String content = (String) message.get("content");

            Long userId = (userIdObj instanceof Number) ? ((Number) userIdObj).longValue() : Long.parseLong(userIdObj.toString());
            Long counselorId = (counselorIdObj instanceof Number) ? ((Number) counselorIdObj).longValue() : Long.parseLong(counselorIdObj.toString());
            Long appointmentId = (appointmentIdObj instanceof Number) ? ((Number) appointmentIdObj).longValue() : null;

            String sender;
            if ("user".equals(userType)) {
                sender = "user";
            } else {
                counselorId = currentUserId;
                sender = "counselor";
            }

            if (userId == null || counselorId == null || content == null || content.trim().isEmpty()) {
                return Result.error("参数不完整");
            }

            // 权限检查：直接消息咨询师不可回复
            boolean isDirect = appointmentId == null || appointmentId <= 0;
            if ("counselor".equals(sender) && isDirect) {
                return Result.error("直接消息不能回复，请等待用户预约后再沟通");
            }

            // 权限检查：预约已结束/已取消则不可回复
            if (appointmentId != null && appointmentId > 0) {
                Appointment apt = appointmentMapper.selectById(appointmentId);
                if (apt != null && (Integer.valueOf(2).equals(apt.getStatus()) || Integer.valueOf(3).equals(apt.getStatus()))) {
                    return Result.error("预约已" + (apt.getStatus() == 2 ? "完成" : "取消") + "，不能继续发送消息");
                }
                if (apt != null && Integer.valueOf(3).equals(apt.getServiceType())) {
                    return Result.error("门诊预约请线下咨询，不支持在线沟通");
                }
            }

            ChatMessage saved = chatMessageService.sendMessage(userId, counselorId, appointmentId, sender, content);

            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "new_message");
            pushData.put("messageId", saved.getId());
            pushData.put("userId", userId);
            pushData.put("counselorId", counselorId);
            pushData.put("content", content);
            pushData.put("sender", sender);
            pushData.put("createTime", saved.getCreateTime() != null ? saved.getCreateTime().toString() : "");
            pushData.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            ChatWebSocketHandler.pushMessage(userId, pushData);
            ChatWebSocketHandler.pushMessage(counselorId, pushData);

            return Result.success("消息发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }

    @PutMapping("/read")
    public Result<String> markAsRead(
            @RequestBody Map<String, Long> body,
            @RequestAttribute(value = "userId", required = false) Long currentUserId,
            @RequestAttribute(value = "userType", required = false) String userType) {

        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        Long userId = body.get("userId");
        Long counselorId = body.get("counselorId");
        if (userId == null || counselorId == null) {
            return Result.error("参数不完整");
        }

        String readerType = "counselor".equals(userType) ? "counselor" : "user";
        chatMessageService.markAsRead(userId, counselorId, readerType);
        return Result.success("已标记已读");
    }

    @PutMapping("/messages/{id}/delete")
    public Result<String> deleteMessage(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long currentUserId,
            @RequestAttribute(value = "userType", required = false) String userType) {

        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        if (!"user".equals(userType) && !"counselor".equals(userType)) {
            return Result.error("权限不足");
        }

        chatMessageService.deleteMessage(id, userType);
        return Result.success("消息已删除");
    }
}
