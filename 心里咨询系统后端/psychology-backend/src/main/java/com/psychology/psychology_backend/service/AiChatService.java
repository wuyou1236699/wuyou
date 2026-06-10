package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.psychology_backend.entity.AiChatLog;
import com.psychology.psychology_backend.mapper.AiChatLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AiChatService {

    @Autowired
    private SparkWebSocketClient sparkClient;

    @Autowired
    private AiChatLogMapper aiChatLogMapper;

    private static final List<String> RISK_KEYWORDS = List.of("自杀", "想死", "不想活了", "结束生命", "活不下去");
    private static final Pattern RISK_PATTERN = Pattern.compile(String.join("|", RISK_KEYWORDS));

    public String chat(Long userId, String userMessage) {
        int riskFlag = RISK_PATTERN.matcher(userMessage).find() ? 1 : 0;

        String aiResponse = sparkClient.chat(userMessage);

        AiChatLog userLog = new AiChatLog();
        userLog.setUserId(userId);
        userLog.setRole("user");
        userLog.setContent(userMessage);
        aiChatLogMapper.insert(userLog);

        AiChatLog aiLog = new AiChatLog();
        aiLog.setUserId(userId);
        aiLog.setRole("ai");
        aiLog.setContent(aiResponse);
        aiChatLogMapper.insert(aiLog);

        if (riskFlag == 1) {
            System.out.println("[风险预警] userId=" + userId + ", message=" + userMessage);
        }
        return aiResponse;
    }

    public List<AiChatLog> getHistory(Long userId, int limit) {
        LambdaQueryWrapper<AiChatLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatLog::getUserId, userId)
                .orderByDesc(AiChatLog::getCreateTime)
                .last("LIMIT " + limit);
        return aiChatLogMapper.selectList(wrapper);
    }

    public void clearHistory(Long userId) {
        LambdaQueryWrapper<AiChatLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatLog::getUserId, userId);
        aiChatLogMapper.delete(wrapper);
    }
}
