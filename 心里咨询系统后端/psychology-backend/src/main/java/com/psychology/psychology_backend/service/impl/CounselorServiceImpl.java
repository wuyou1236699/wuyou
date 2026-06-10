package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.dto.CounselorRecommendDTO;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.mapper.CounselorMapper;
import com.psychology.psychology_backend.service.AppointmentService;
import com.psychology.psychology_backend.service.CounselorService;
import com.psychology.psychology_backend.service.ReviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CounselorServiceImpl extends ServiceImpl<CounselorMapper, Counselor> implements CounselorService {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AppointmentService appointmentService;

    @Override
    public List<CounselorRecommendDTO> recommend(String problemType) {
        List<Counselor> counselors = lambdaQuery().eq(Counselor::getStatus, 1).list();
        List<CounselorRecommendDTO> result = new ArrayList<>();

        // 先把全局 max 算出来，用来归一化
        double maxRating = 0;
        double maxLogCount = 0;
        for (Counselor c : counselors) {
            Double r = reviewService.getAverageRating(c.getId());
            if (r != null && r > maxRating) maxRating = r;
            long cnt = appointmentService.lambdaQuery().eq(Appointment::getCounselorId, c.getId()).count();
            double logCnt = Math.log1p(cnt);
            if (logCnt > maxLogCount) maxLogCount = logCnt;
        }

        for (Counselor c : counselors) {
            CounselorRecommendDTO dto = new CounselorRecommendDTO();
            BeanUtils.copyProperties(c, dto);

            // 匹配度：按关键词命中比例打分（0~1）
            double matchRatio = 0.5; // 默认无病情时中等匹配
            if (problemType != null && !problemType.isEmpty()) {
                String expertise = c.getExpertise();
                if (expertise != null && !expertise.isEmpty()) {
                    String[] keywords = problemType.split("[，,、/\\s]+");
                    int hit = 0;
                    for (String kw : keywords) {
                        if (!kw.isBlank() && expertise.contains(kw.trim())) hit++;
                    }
                    matchRatio = keywords.length > 0 ? (double) hit / keywords.length : 0.5;
                } else {
                    matchRatio = 0;
                }
            }

            // 评分归一化 (0~1)
            Double avgRating = reviewService.getAverageRating(c.getId());
            if (avgRating == null) avgRating = 0.0;
            double ratingNorm = maxRating > 0 ? avgRating / maxRating : 0;

            // 经验归一化：log(1 + 预约数) / maxLogCount (0~1)
            long appointmentCount = appointmentService.lambdaQuery()
                    .eq(Appointment::getCounselorId, c.getId()).count();
            double expNorm = maxLogCount > 0 ? Math.log1p(appointmentCount) / maxLogCount : 0;

            // 综合得分：匹配 50% + 口碑 30% + 经验 20%
            double totalScore = matchRatio * 50 + ratingNorm * 30 + expNorm * 20;
            dto.setMatchScore(Math.round(totalScore * 10) / 10.0);
            dto.setAvgRating(Math.round(avgRating * 10) / 10.0);
            dto.setAppointmentCount((int) appointmentCount);
            result.add(dto);
        }
        result.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        int limit = Math.min(3, result.size());
        return result.subList(0, limit);
    }
}
