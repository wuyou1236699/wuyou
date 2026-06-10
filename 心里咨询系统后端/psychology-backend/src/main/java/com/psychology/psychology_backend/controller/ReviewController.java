package com.psychology.psychology_backend.controller;

import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Review;
import com.psychology.psychology_backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public Result<String> addReview(@RequestAttribute Long userId, @RequestBody Review review) {
        review.setUserId(userId);
        if (reviewService.addReview(review)) {
            return Result.success("评价成功");
        }
        return Result.error("评价失败");
    }

    @GetMapping("/list")
    public Result<List<Review>> list(@RequestParam Long counselorId,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        List<Review> reviews = reviewService.getReviewsByCounselor(counselorId, page, size);
        System.out.println("返回的评价数据: " + reviews);
        return Result.success(reviews);
    }

    @GetMapping("/avg")
    public Result<Double> avg(@RequestParam Long counselorId) {
        return Result.success(reviewService.getAverageRating(counselorId));
    }

    @GetMapping("/my")
    public Result<List<Review>> getMyReviews(@RequestAttribute Long userId) {
        List<Review> reviews = reviewService.getMyReviews(userId);
        return Result.success(reviews);
    }
}