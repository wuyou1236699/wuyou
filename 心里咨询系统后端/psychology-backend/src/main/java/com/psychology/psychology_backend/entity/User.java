package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String openid;
    private String nickname;
    private String avatar;
    private String status;
    private String phone;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
