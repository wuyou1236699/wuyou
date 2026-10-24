package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psychology.psychology_backend.entity.Admin;

public interface AdminService extends IService<Admin> {
    String adminLogin(String username, String password, String ip);
}