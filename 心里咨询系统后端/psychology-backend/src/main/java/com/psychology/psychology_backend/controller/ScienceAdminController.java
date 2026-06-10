package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.ScienceArticle;
import com.psychology.psychology_backend.entity.ScienceCategory;
import com.psychology.psychology_backend.service.ScienceArticleService;
import com.psychology.psychology_backend.service.ScienceCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/science")
public class ScienceAdminController {

    @Autowired
    private ScienceCategoryService scienceCategoryService;

    @Autowired
    private ScienceArticleService scienceArticleService;

    // ===== 分类管理 =====

    @GetMapping("/categories")
    public Result<List<ScienceCategory>> getCategories() {
        return Result.success(scienceCategoryService.list(new QueryWrapper<ScienceCategory>().orderByAsc("sort")));
    }

    @PostMapping("/categories")
    public Result<ScienceCategory> createCategory(@RequestBody ScienceCategory category) {
        scienceCategoryService.save(category);
        return Result.success(category);
    }

    @PutMapping("/categories/{id}")
    public Result<ScienceCategory> updateCategory(@PathVariable Long id, @RequestBody ScienceCategory category) {
        category.setId(id);
        scienceCategoryService.updateById(category);
        return Result.success(category);
    }

    @DeleteMapping("/categories/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        scienceCategoryService.removeById(id);
        return Result.success("删除成功");
    }

    // ===== 文章管理 =====

    @GetMapping("/articles")
    public Result<Map<String, Object>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId) {

        QueryWrapper<ScienceArticle> wrapper = new QueryWrapper<>();
        if (categoryId != null) wrapper.eq("category_id", categoryId);
        wrapper.orderByDesc("create_time");

        List<ScienceArticle> all = scienceArticleService.list(wrapper);
        int total = all.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<ScienceArticle> records = start < end ? all.subList(start, end) : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));
        return Result.success(result);
    }

    @GetMapping("/articles/{id}")
    public Result<ScienceArticle> getArticle(@PathVariable Long id) {
        return Result.success(scienceArticleService.getById(id));
    }

    @PostMapping("/articles")
    public Result<ScienceArticle> createArticle(@RequestBody ScienceArticle article) {
        article.setCreateTime(LocalDateTime.now());
        article.setViewCount(0);
        article.setLikeCount(0);
        scienceArticleService.save(article);
        return Result.success(article);
    }

    @PutMapping("/articles/{id}")
    public Result<ScienceArticle> updateArticle(@PathVariable Long id, @RequestBody ScienceArticle article) {
        article.setId(id);
        scienceArticleService.updateById(article);
        return Result.success(article);
    }

    @DeleteMapping("/articles/{id}")
    public Result<String> deleteArticle(@PathVariable Long id) {
        scienceArticleService.removeById(id);
        return Result.success("删除成功");
    }
}
