package com.psychology.psychology_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.psychology.psychology_backend.entity.Review;
import java.util.List;

public interface ReviewService extends IService<Review> {
    boolean addReview(Review review);
    List<Review> getReviewsByCounselor(Long counselorId, int page, int size);
    Double getAverageRating(Long counselorId);

    List<Review> getMyReviews(Long userId);
}