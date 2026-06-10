package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("science_article")
public class ScienceArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String title;

    private String summary;

    private String content;

    private String cover;

    private Integer viewCount;

    private Integer likeCount;

    private LocalDateTime createTime;
}
