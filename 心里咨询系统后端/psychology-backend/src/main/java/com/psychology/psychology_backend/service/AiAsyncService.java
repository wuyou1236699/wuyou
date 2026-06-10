package com.psychology.psychology_backend.service;

import com.psychology.psychology_backend.entity.AiTask;
import com.psychology.psychology_backend.mapper.AiTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AiAsyncService {

    @Autowired
    private SparkWebSocketClient sparkClient;
    @Autowired
    private AiTaskMapper aiTaskMapper;

    @Async
    public void processTask(Long taskId, String userMessage) {
        String reply = sparkClient.chat(userMessage);
        AiTask task = new AiTask();
        task.setId(taskId);
        task.setAiResponse(reply);
        // 判断是否成功（可根据实际返回内容调整）
        task.setStatus(reply != null && !reply.startsWith("AI 服务错误") ? 1 : 2);
        aiTaskMapper.updateById(task);
    }
}