package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.ScienceArticle;
import com.psychology.psychology_backend.entity.ScienceCategory;
import com.psychology.psychology_backend.service.ScienceArticleService;
import com.psychology.psychology_backend.service.ScienceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/science")
public class ScienceController {

    @Autowired
    private ScienceCategoryService scienceCategoryService;

    @Autowired
    private ScienceArticleService scienceArticleService;

    @GetMapping("/categories")
    public Result<List<ScienceCategory>> getCategories() {
        QueryWrapper<ScienceCategory> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort");
        List<ScienceCategory> categories = scienceCategoryService.list(wrapper);
        return Result.success(categories);
    }

    @GetMapping("/articles")
    public Result<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId) {
        
        int offset = (page - 1) * size;
        QueryWrapper<ScienceArticle> wrapper = new QueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        
        wrapper.orderByDesc("create_time");
        
        List<ScienceArticle> allArticles = scienceArticleService.list(wrapper);
        long total = allArticles.size();
        
        List<ScienceArticle> articles = allArticles.stream()
                .skip(offset)
                .limit(size)
                .toList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", articles);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        
        return Result.success(result);
    }

    @GetMapping("/articles/{id}")
    public Result<ScienceArticle> getArticleDetail(@PathVariable Long id) {
        ScienceArticle article = scienceArticleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        article.setViewCount(article.getViewCount() + 1);
        scienceArticleService.updateById(article);
        
        return Result.success(article);
    }

    @PostMapping("/articles/{id}/like")
    public Result<String> likeArticle(@PathVariable Long id) {
        ScienceArticle article = scienceArticleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        
        article.setLikeCount(article.getLikeCount() + 1);
        scienceArticleService.updateById(article);
        
        return Result.success("点赞成功");
    }
}
