package com.psychology.psychology_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.entity.Review;
import com.psychology.psychology_backend.entity.User;
import com.psychology.psychology_backend.mapper.ReviewMapper;
import com.psychology.psychology_backend.service.AppointmentService;
import com.psychology.psychology_backend.service.CounselorService;
import com.psychology.psychology_backend.service.ReviewService;
import com.psychology.psychology_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    @Lazy
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CounselorService counselorService;

    @Override
    public boolean addReview(Review review) {
        Appointment apt = appointmentService.getById(review.getAppointmentId());
        if (apt == null || apt.getStatus() != 2) {
            throw new RuntimeException("只能对已完成的咨询进行评价");
        }
        long count = lambdaQuery().eq(Review::getAppointmentId, review.getAppointmentId()).count();
        if (count > 0) {
            throw new RuntimeException("该预约已评价过");
        }
        return save(review);
    }

    @Override
    public List<Review> getReviewsByCounselor(Long counselorId, int page, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getCounselorId, counselorId)
                .orderByDesc(Review::getCreateTime);
        List<Review> reviews = list(wrapper);
        
        System.out.println("查询到的评价原始数据: " + reviews);
        
        // 填充用户名称
        for (Review review : reviews) {
            User user = userService.getById(review.getUserId());
            if (user != null) {
                review.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        System.out.println("处理后的评价数据: " + reviews);
        return reviews;
    }

    @Override
    public Double getAverageRating(Long counselorId) {
        List<Review> reviews = lambdaQuery().eq(Review::getCounselorId, counselorId).list();
        if (reviews.isEmpty()) return 0.0;
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }

    @Override
    public List<Review> getMyReviews(Long userId) {
        List<Review> reviews = lambdaQuery()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime)
                .list();

        for (Review review : reviews) {
            Counselor counselor = counselorService.getById(review.getCounselorId());
            if (counselor != null) {
                review.setCounselorName(counselor.getName());
            }
        }
        return reviews;
    }
}