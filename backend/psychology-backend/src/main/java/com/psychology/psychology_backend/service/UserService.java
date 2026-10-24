package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psychology.psychology_backend.entity.User;

public interface UserService extends IService<User> {
    String wxLogin(String openid);
    String generateToken(Long userId);
}
