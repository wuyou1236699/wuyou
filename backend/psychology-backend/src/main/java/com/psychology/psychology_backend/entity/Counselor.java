package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("counselor")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private Integer gender;
    private String qualification;
    private String expertise;
    private String phone;
    private String email;
    private String profile;
    private String avatar;
    private Integer status;
    @TableField(exist = false)
    private Double rating;
    @TableField(value = "consult_count", exist = false)
    private Integer consultCount;
}