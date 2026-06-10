package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.entity.ScienceArticle;
import com.psychology.psychology_backend.mapper.ScienceArticleMapper;
import com.psychology.psychology_backend.service.ScienceArticleService;
import org.springframework.stereotype.Service;

@Service
public class ScienceArticleServiceImpl extends ServiceImpl<ScienceArticleMapper, ScienceArticle> implements ScienceArticleService {
}
