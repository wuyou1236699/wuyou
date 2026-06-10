package com.psychology.psychology_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("consultation_record")
public class ConsultationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long userId;
    private Long counselorId;
    private String problem;
    private String diagnosis;
    private String suggestions;
    private Integer duration;
    private BigDecimal fee;
    private Integer status; // 0草稿 1已完成
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
