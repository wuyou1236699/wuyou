package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("test_option")
public class TestOption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private Integer optionOrder;
    private String optionText;
    private Integer score;
}
