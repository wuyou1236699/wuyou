package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.entity.TestRecord;
import com.psychology.psychology_backend.mapper.TestRecordMapper;
import com.psychology.psychology_backend.service.TestRecordService;
import org.springframework.stereotype.Service;

@Service
public class TestRecordServiceImpl extends ServiceImpl<TestRecordMapper, TestRecord> implements TestRecordService {
}
