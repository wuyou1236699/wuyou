package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psychology.psychology_backend.entity.User;
import com.psychology.psychology_backend.mapper.UserMapper;
import com.psychology.psychology_backend.service.UserService;
import com.psychology.psychology_backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${wx.appid:mock_appid}")
    private String appId;

    @Value("${wx.secret:mock_secret}")
    private String appSecret;

    @Override
    public String wxLogin(String code) {
        String openid = getOpenidFromWechat(code);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("用户" + openid.substring(0, Math.min(8, openid.length())));
            this.save(user);
        }

        return generateToken(user.getId());
    }

    @Override
    public String generateToken(Long userId) {
        return jwtUtil.generateToken(userId, "user");
    }

    private String getOpenidFromWechat(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session" +
                "?appid=" + appId +
                "&secret=" + appSecret +
                "&js_code=" + code +
                "&grant_type=authorization_code";
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);
            String openid = jsonNode.get("openid").asText();
            if (openid == null || openid.isEmpty()) {
                throw new RuntimeException("微信登录失败：" + jsonNode.get("errmsg").asText());
            }
            return openid;
        } catch (Exception e) {
            throw new RuntimeException("换取 openid 失败：" + e.getMessage());
        }
    }
}
