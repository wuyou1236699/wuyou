package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_record")
public class TestRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String testType;

    private String testName;

    private Integer totalScore;

    private String result;

    private String suggestions;

    private LocalDateTime createTime;
}
