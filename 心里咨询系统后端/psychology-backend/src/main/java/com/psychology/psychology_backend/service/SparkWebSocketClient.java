package com.psychology.psychology_backend.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.psychology.psychology_backend.config.XfConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class SparkWebSocketClient {

    @Autowired
    private XfConfig xfConfig;

    private StringBuilder fullContent = new StringBuilder();

    public String chat(String userMessage) {
        try {
            fullContent.setLength(0); // 重置内容
            System.out.println("SparkWebSocketClient.chat 开始，消息：" + userMessage);
            String authUrl = getAuthUrl();
            System.out.println("认证 URL 生成成功: " + authUrl);

            CountDownLatch latch = new CountDownLatch(1);
            final String[] response = {""};

            WebSocketClient client = new WebSocketClient(URI.create(authUrl)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("WebSocket 连接已打开，发送请求...");
                    JSONObject request = new JSONObject();
                    request.put("header", new JSONObject() {{
                        put("app_id", xfConfig.getAppid());
                        put("uid", UUID.randomUUID().toString().substring(0, 10));
                    }});
                    JSONObject parameter = new JSONObject();
                    parameter.put("chat", new JSONObject() {{
                        put("domain", "4.0Ultra");        // Ultra-32K 使用 ultra
                        put("temperature", 0.7);
                        put("max_tokens", 1024);
                    }});
                    request.put("parameter", parameter);
                    JSONObject payload = new JSONObject();
                    payload.put("message", new JSONObject() {{
                        put("text", Arrays.asList(
                                new JSONObject() {{ put("role", "user"); put("content", userMessage); }}
                        ));
                    }});
                    request.put("payload", payload);
                    send(request.toJSONString());
                    System.out.println("请求已发送: " + request.toJSONString());
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("收到消息片段: " + message);
                    JSONObject resp = JSON.parseObject(message);
                    if (resp.containsKey("payload") && resp.getJSONObject("payload").containsKey("choices")) {
                        JSONArray textArray = resp.getJSONObject("payload")
                                .getJSONObject("choices")
                                .getJSONArray("text");
                        for (int i = 0; i < textArray.size(); i++) {
                            String content = textArray.getJSONObject(i).getString("content");
                            fullContent.append(content);
                        }
                        int status = resp.getJSONObject("payload").getJSONObject("choices").getIntValue("status");
                        if (status == 2) { // 结束标志
                            response[0] = fullContent.toString();
                            latch.countDown();
                            close();
                        }
                    } else if (resp.containsKey("header") && resp.getJSONObject("header").getIntValue("code") != 0) {
                        response[0] = "AI 服务错误: " + resp.getJSONObject("header").getString("message");
                        latch.countDown();
                        close();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket 连接关闭: " + reason);
                    latch.countDown();
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket 错误: " + ex.getMessage());
                    ex.printStackTrace();
                    response[0] = "网络连接异常";
                    latch.countDown();
                }
            };
            client.connect();
            boolean finished = latch.await(30, TimeUnit.SECONDS);
            if (!finished) {
                client.close();
                return "AI 响应超时，请稍后再试";
            }
            return response[0].isEmpty() ? "AI 暂时无法回复，请稍后再试" : response[0];
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 服务异常: " + e.getMessage();
        }
    }

    private String getAuthUrl() throws Exception {
        String hostUrl = xfConfig.getHostUrl();
        String apiKey = xfConfig.getApiKey();
        String apiSecret = xfConfig.getApiSecret();

        URI uri = new URI(hostUrl);
        String host = uri.getHost();
        String path = uri.getPath();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = sdf.format(new Date());

        String preStr = "host: " + host + "\n" +
                "date: " + date + "\n" +
                "GET " + path + " HTTP/1.1";

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(spec);
        byte[] signatureBytes = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signatureBytes);

        String authorization = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                apiKey, signature);
        String encodedAuth = Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));

        return hostUrl + "?authorization=" + encodedAuth + "&date=" + java.net.URLEncoder.encode(date, "UTF-8") + "&host=" + host;
    }
}