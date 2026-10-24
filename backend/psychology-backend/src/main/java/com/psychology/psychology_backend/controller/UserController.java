package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.User;
import com.psychology.psychology_backend.service.UserService;
import com.psychology.psychology_backend.utils.PasswordUtil;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordUtil passwordUtil;

    private static final Map<Long, Boolean> userOnlineStatusMap = new HashMap<>();

    @PostMapping("/wx-login")
    public Result<String> wxLogin(@RequestBody WxLoginRequest request) {
        try {
            String token = userService.wxLogin(request.getCode());
            return Result.success(token);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        try {
            // 检查用户名是否已存在
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername());
            User existUser = userService.getOne(wrapper);
            if (existUser != null) {
                return Result.error("用户名已存在");
            }

            // 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordUtil.encode(request.getPassword()));
            user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
            user.setAvatar("/images/default-avatar.png");
            user.setOpenid("web_" + request.getUsername());
            user.setStatus("active");
            userService.save(user);

            // 生成 token
            String token = userService.generateToken(user.getId());

            // 用户注册后默认设为在线
            userOnlineStatusMap.put(user.getId(), true);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername());
            User user = userService.getOne(wrapper);

            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            // 验证密码：BCrypt优先，明文兼容（自动升级）
            String storedPwd = user.getPassword();
            boolean matched;
            if (storedPwd != null && (storedPwd.startsWith("$2a$") || storedPwd.startsWith("$2b$"))) {
                matched = passwordUtil.matches(request.getPassword(), storedPwd);
            } else {
                matched = request.getPassword().equals(storedPwd);
                if (matched) {
                    user.setPassword(passwordUtil.encode(request.getPassword()));
                    userService.updateById(user);
                }
            }
            if (!matched) {
                return Result.error("用户名或密码错误");
            }

            if ("inactive".equals(user.getStatus()) || "0".equals(user.getStatus())) {
                return Result.error("账号已被禁用");
            }

            // 生成 token
            String token = userService.generateToken(user.getId());

            // 用户登录后默认设为在线
            userOnlineStatusMap.put(user.getId(), true);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatar", user.getAvatar());

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestAttribute(value = "userId", required = false) Long userId,
                                        @RequestBody Map<String, String> body) {
        if (userId == null) return Result.error("请先登录");
        User user = userService.getById(userId);
        if (user == null) return Result.error("用户不存在");
        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @GetMapping("/online-status/{userId}")
    public Result<Map<String, Object>> getUserOnlineStatus(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        Boolean isOnline = userOnlineStatusMap.getOrDefault(userId, false);
        result.put("isOnline", isOnline);
        return Result.success(result);
    }

    @GetMapping("/online-status-list")
    public Result<Map<Long, Boolean>> getAllUserOnlineStatus() {
        return Result.success(userOnlineStatusMap);
    }

    @PutMapping("/online-status")
    public Result<String> setUserOnlineStatus(@RequestAttribute(value = "userId", required = false) Long userId,
                                               @RequestBody Map<String, Boolean> body) {
        Boolean isOnline = body.get("isOnline");
        if (userId != null) {
            if (isOnline != null && isOnline) {
                userOnlineStatusMap.put(userId, true);
                return Result.success("已上线");
            } else {
                userOnlineStatusMap.put(userId, false);
                return Result.success("已离线");
            }
        }
        return Result.error("请先登录");
    }

    @Data
    static class WxLoginRequest {
        private String code;
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
