package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.entity.ScienceCategory;
import com.psychology.psychology_backend.mapper.ScienceCategoryMapper;
import com.psychology.psychology_backend.service.ScienceCategoryService;
import org.springframework.stereotype.Service;

@Service
public class ScienceCategoryServiceImpl extends ServiceImpl<ScienceCategoryMapper, ScienceCategory> implements ScienceCategoryService {
}
