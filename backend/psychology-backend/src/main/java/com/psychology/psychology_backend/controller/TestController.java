package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<String> test() {
        return Result.success("Backend is running!");
    }

    // 临时接口，生成测试 token（用户id=1, 角色ROLE_USER）
    @GetMapping("/token")
    public Result<String> getTestToken() {
        String token = jwtUtil.generateToken(1L, "ROLE_USER");
        return Result.success(token);
    }

    // 临时接口，生成 BCRYPT 哈希（用于管理员密码）
    @GetMapping("/hash")
    public Result<String> getPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("admin123"); // 根据需要修改密码
        return Result.success(hash);
    }
}