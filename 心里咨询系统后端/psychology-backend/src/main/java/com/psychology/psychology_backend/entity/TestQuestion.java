package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("test_question")
public class TestQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer testId;
    private String questionText;
    private Integer sortOrder;
}
