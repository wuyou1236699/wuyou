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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tests")
public class TestsController {

    @Autowired
    private TestRecordService testRecordService;

    @Autowired
    private TestQuestionMapper testQuestionMapper;

    @Autowired
    private TestOptionMapper testOptionMapper;

    @GetMapping
    public Result<List<Map<String, Object>>> getTests() {
        List<Map<String, Object>> tests = new ArrayList<>();
        tests.add(createTest(1, "抑郁自评量表(SDS)", "depression", "评估您的抑郁程度", "20题", "3-5分钟"));
        tests.add(createTest(2, "焦虑自评量表(SAS)", "anxiety", "评估您的焦虑程度", "20题", "3-5分钟"));
        tests.add(createTest(3, "压力测试", "stress", "评估您的压力水平", "15题", "2-3分钟"));
        tests.add(createTest(4, "情绪管理测试", "emotion", "评估您的情绪管理能力", "25题", "5分钟"));
        tests.add(createTest(5, "睡眠质量测试", "sleep", "评估您的睡眠状况", "10题", "2分钟"));
        tests.add(createTest(6, "人际关系测试", "relationship", "评估您的人际关系", "20题", "4分钟"));
        return Result.success(tests);
    }

    @GetMapping("/{id}/questions")
    public Result<List<Map<String, Object>>> getQuestions(@PathVariable Integer id) {
        List<TestQuestion> questions = testQuestionMapper.selectList(
            new LambdaQueryWrapper<TestQuestion>()
                .eq(TestQuestion::getTestId, id)
                .orderByAsc(TestQuestion::getSortOrder)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (TestQuestion q : questions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("question", q.getQuestionText());

            List<TestOption> options = testOptionMapper.selectList(
                new LambdaQueryWrapper<TestOption>()
                    .eq(TestOption::getQuestionId, q.getId())
                    .orderByAsc(TestOption::getOptionOrder)
            );
            List<Map<String, Object>> optionList = new ArrayList<>();
            for (TestOption opt : options) {
                Map<String, Object> om = new HashMap<>();
                om.put("id", opt.getOptionOrder());
                om.put("text", opt.getOptionText());
                om.put("score", opt.getScore());
                optionList.add(om);
            }
            map.put("options", optionList);
            result.add(map);
        }
        return Result.success(result);
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getTestHistory(@RequestAttribute(value = "userId", required = false) Long userId) {
        LambdaQueryWrapper<TestRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(TestRecord::getUserId, userId);
        }
        wrapper.orderByDesc(TestRecord::getCreateTime);

        List<TestRecord> records = testRecordService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (TestRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("testType", record.getTestType());
            item.put("testName", record.getTestName());
            item.put("totalScore", record.getTotalScore());
            item.put("result", record.getResult());
            item.put("suggestions", record.getSuggestions());
            item.put("createTime", record.getCreateTime());
            result.add(item);
        }

        return Result.success(result);
    }

    @PostMapping("/submit")
    public Result<Map<String, Object>> submitTest(@RequestAttribute(value = "userId", required = false) Long userId,
                                                  @RequestBody Map<String, Object> body) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        Integer testId = (Integer) body.get("testId");
        String testName = (String) body.get("testName");
        @SuppressWarnings("unchecked")
        Map<String, Integer> answers = ((Map<String, Object>) body.get("answers")).entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> ((Number) e.getValue()).intValue()
                ));

        List<Map<String, Object>> questions = getQuestionsById(testId);
        int totalScore = 0;

        for (Map<String, Object> question : questions) {
            String qId = String.valueOf(question.get("id"));
            Integer selectedOptionId = answers.get(qId);
            if (selectedOptionId != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> options = (List<Map<String, Object>>) question.get("options");
                for (Map<String, Object> option : options) {
                    if (option.get("id").equals(selectedOptionId)) {
                        totalScore += ((Number) option.get("score")).intValue();
                        break;
                    }
                }
            }
        }

        int maxScore = questions.size() * 5;
        String level;
        String suggestions;

        if (totalScore <= maxScore * 0.25) {
            level = "正常";
            suggestions = "您的心理状态良好！继续保持健康的生活方式和积极的心态。";
        } else if (totalScore <= maxScore * 0.40) {
            level = "轻度";
            suggestions = "您可能有轻微的心理困扰，可以尝试通过运动、冥想等方式进行自我调节。";
        } else if (totalScore <= maxScore * 0.55) {
            level = "中度";
            suggestions = "您的心理困扰较为明显，建议寻求专业心理咨询师的帮助。";
        } else {
            level = "重度";
            suggestions = "您的心理困扰比较严重，强烈建议尽快联系心理医生进行专业评估和治疗。";
        }

        TestRecord record = new TestRecord();
        record.setUserId(userId);
        record.setTestType(questions.isEmpty() ? "" : (String) questions.get(0).get("question"));
        record.setTestName(testName);
        record.setTotalScore(totalScore);
        record.setResult(level);
        record.setSuggestions(suggestions);
        testRecordService.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("score", totalScore);
        result.put("level", level);
        result.put("suggestions", suggestions);
        result.put("maxScore", maxScore);

        return Result.success(result);
    }

    private List<Map<String, Object>> getQuestionsById(Integer testId) {
        List<TestQuestion> questions = testQuestionMapper.selectList(
            new LambdaQueryWrapper<TestQuestion>()
                .eq(TestQuestion::getTestId, testId)
                .orderByAsc(TestQuestion::getSortOrder)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (TestQuestion q : questions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("question", q.getQuestionText());
            List<TestOption> options = testOptionMapper.selectList(
                new LambdaQueryWrapper<TestOption>()
                    .eq(TestOption::getQuestionId, q.getId())
                    .orderByAsc(TestOption::getOptionOrder)
            );
            List<Map<String, Object>> optionList = new ArrayList<>();
            for (TestOption opt : options) {
                Map<String, Object> om = new HashMap<>();
                om.put("id", opt.getOptionOrder());
                om.put("text", opt.getOptionText());
                om.put("score", opt.getScore());
                optionList.add(om);
            }
            map.put("options", optionList);
            result.add(map);
        }
        return result;
    }

    @GetMapping("/types")
    public Result<List<Map<String, Object>>> getTestTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        types.add(createType("depression", "抑郁自评量表", "评估抑郁程度"));
        types.add(createType("anxiety", "焦虑自评量表", "评估焦虑程度"));
        types.add(createType("stress", "压力测试", "评估压力水平"));
        types.add(createType("sleep", "睡眠质量测试", "评估睡眠状况"));
        return Result.success(types);
    }

    private Map<String, Object> createQuestion(int id, String content) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("question", content);
        
        List<Map<String, Object>> options = new ArrayList<>();
        String[] optionTexts = {"完全不符合", "不太符合", "一般", "比较符合", "完全符合"};
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", i);
            option.put("text", optionTexts[i - 1]);
            option.put("score", i);
            options.add(option);
        }
        map.put("options", options);
        return map;
    }

    private Map<String, Object> createTest(int id, String title, String category, String description, String questions, String duration) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("category", category);
        map.put("description", description);
        map.put("questions", questions);
        map.put("duration", duration);
        return map;
    }

    private Map<String, Object> createType(String type, String name, String description) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("name", name);
        map.put("description", description);
        return map;
    }
}
