package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.entity.ConsultationRecord;
import com.psychology.psychology_backend.mapper.ConsultationRecordMapper;
import com.psychology.psychology_backend.service.ConsultationRecordService;
import org.springframework.stereotype.Service;

@Service
public class ConsultationRecordServiceImpl extends ServiceImpl<ConsultationRecordMapper, ConsultationRecord> implements ConsultationRecordService {
}
