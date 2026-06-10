package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.AiTask;
import com.psychology.psychology_backend.mapper.AiTaskMapper;
import com.psychology.psychology_backend.service.AiAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiAsyncController {

    @Autowired
    private AiTaskMapper aiTaskMapper;
    @Autowired
    private AiAsyncService aiAsyncService;

    @PostMapping("/chat/async")
    public Result<Long> chatAsync(@RequestAttribute Long userId, @RequestBody Map<String, String> body) {
        String userMessage = body.get("message");
        AiTask task = new AiTask();
        task.setUserId(userId);
        task.setUserMessage(userMessage);
        task.setStatus(0); // 处理中
        aiTaskMapper.insert(task);
        aiAsyncService.processTask(task.getId(), userMessage);
        return Result.success(task.getId());
    }

    @GetMapping("/task/{taskId}")
    public Result<String> getTaskResult(@PathVariable Long taskId, @RequestAttribute Long userId) {
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            return Result.error("任务不存在");
        }
        if (task.getStatus() == 1) {
            return Result.success(task.getAiResponse());
        } else if (task.getStatus() == 2) {
            return Result.error("AI 处理失败");
        } else {
            return Result.error("处理中");
        }
    }
}