package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.AiChatLog;
import com.psychology.psychology_backend.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestAttribute Long userId, @RequestBody Map<String, String> body) {
        System.out.println("=== AiController.chat 被调用，userId=" + userId + ", message=" + body.get("message"));
        String userMessage = body.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        String reply = aiChatService.chat(userId, userMessage);
        return Result.success(reply);
    }

    @GetMapping("/history")
    public Result<List<AiChatLog>> history(@RequestAttribute Long userId,
                                           @RequestParam(defaultValue = "20") int limit) {
        return Result.success(aiChatService.getHistory(userId, limit));
    }

    @DeleteMapping("/history")
    public Result<Void> clearHistory(@RequestAttribute Long userId) {
        aiChatService.clearHistory(userId);
        return Result.success(null);
    }
}