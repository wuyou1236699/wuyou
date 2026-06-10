package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.TestOption;
import com.psychology.psychology_backend.entity.TestQuestion;
import com.psychology.psychology_backend.entity.TestRecord;
import com.psychology.psychology_backend.mapper.TestOptionMapper;
import com.psychology.psychology_backend.mapper.TestQuestionMapper;
import com.psychology.psychology_backend.service.TestRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/tests")
public class TestAdminController {

    @Autowired
    private TestQuestionMapper testQuestionMapper;

    @Autowired
    private TestOptionMapper testOptionMapper;

    @Autowired
    private TestRecordService testRecordService;

    @GetMapping("/questions")
    public Result<List<Map<String, Object>>> getQuestions(@RequestParam Integer testId) {
        List<TestQuestion> questions = testQuestionMapper.selectList(
            new LambdaQueryWrapper<TestQuestion>()
                .eq(TestQuestion::getTestId, testId)
                .orderByAsc(TestQuestion::getSortOrder));

        List<Map<String, Object>> result = new ArrayList<>();
        for (TestQuestion q : questions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("testId", q.getTestId());
            map.put("questionText", q.getQuestionText());
            map.put("sortOrder", q.getSortOrder());

            List<TestOption> options = testOptionMapper.selectList(
                new LambdaQueryWrapper<TestOption>()
                    .eq(TestOption::getQuestionId, q.getId())
                    .orderByAsc(TestOption::getOptionOrder));
            map.put("options", options);

            result.add(map);
        }
        return Result.success(result);
    }

    @PostMapping("/questions")
    public Result<Map<String, Object>> addQuestion(@RequestBody Map<String, Object> body) {
        TestQuestion q = new TestQuestion();
        q.setTestId((Integer) body.get("testId"));
        q.setQuestionText((String) body.get("questionText"));
        q.setSortOrder((Integer) body.getOrDefault("sortOrder", 1));
        testQuestionMapper.insert(q);

        Map<String, Object> result = new HashMap<>();
        result.put("id", q.getId());
        return Result.success(result);
    }

    @PutMapping("/questions/{id}")
    public Result<String> updateQuestion(@PathVariable Long id, @RequestBody Map<String, String> body) {
        TestQuestion q = testQuestionMapper.selectById(id);
        if (q == null) return Result.error("题目不存在");
        if (body.containsKey("questionText")) q.setQuestionText(body.get("questionText"));
        if (body.containsKey("sortOrder")) q.setSortOrder(Integer.parseInt(body.get("sortOrder")));
        testQuestionMapper.updateById(q);
        return Result.success("更新成功");
    }

    @DeleteMapping("/questions/{id}")
    public Result<String> deleteQuestion(@PathVariable Long id) {
        testOptionMapper.delete(new LambdaQueryWrapper<TestOption>().eq(TestOption::getQuestionId, id));
        testQuestionMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/options")
    public Result<Map<String, Object>> addOption(@RequestBody TestOption option) {
        testOptionMapper.insert(option);
        Map<String, Object> result = new HashMap<>();
        result.put("id", option.getId());
        return Result.success(result);
    }

    @PutMapping("/options/{id}")
    public Result<String> updateOption(@PathVariable Long id, @RequestBody TestOption option) {
        option.setId(id);
        testOptionMapper.updateById(option);
        return Result.success("更新成功");
    }

    @DeleteMapping("/options/{id}")
    public Result<String> deleteOption(@PathVariable Long id) {
        testOptionMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory() {
        List<TestRecord> records = testRecordService.list(
            new LambdaQueryWrapper<TestRecord>().orderByDesc(TestRecord::getCreateTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (TestRecord r : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("userId", r.getUserId());
            map.put("testName", r.getTestName());
            map.put("totalScore", r.getTotalScore());
            map.put("result", r.getResult());
            map.put("suggestions", r.getSuggestions());
            map.put("createTime", r.getCreateTime());
            result.add(map);
        }
        return Result.success(result);
    }
}