package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.entity.Admin;
import com.psychology.psychology_backend.mapper.AdminMapper;
import com.psychology.psychology_backend.service.AdminService;
import com.psychology.psychology_backend.utils.JwtUtil;
import com.psychology.psychology_backend.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Override
    public String adminLogin(String username, String password, String ip) {
        Admin admin = lambdaQuery().eq(Admin::getUsername, username).one();
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String storedPwd = admin.getPassword();
        boolean matched;
        if (storedPwd != null && (storedPwd.startsWith("$2a$") || storedPwd.startsWith("$2b$"))) {
            matched = passwordUtil.matches(password, storedPwd);
        } else {
            matched = password.equals(storedPwd);
            if (matched) {
                admin.setPassword(passwordUtil.encode(password));
                updateById(admin);
            }
        }

        if (!matched) {
            throw new RuntimeException("用户名或密码错误");
        }

        return jwtUtil.generateToken(admin.getId(), "ROLE_ADMIN");
    }
}