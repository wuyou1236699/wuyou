package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psychology.psychology_backend.dto.CounselorRecommendDTO;
import com.psychology.psychology_backend.entity.Counselor;
import java.util.List;

public interface CounselorService extends IService<Counselor> {
    List<CounselorRecommendDTO> recommend(String problemType);
}