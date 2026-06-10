package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("science_category")
public class ScienceCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String icon;

    private Integer sort;
}
