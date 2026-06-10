package com.psychology.psychology_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.psychology_backend.common.Result;
import com.psychology.psychology_backend.entity.Appointment;
import com.psychology.psychology_backend.entity.ConsultationRecord;
import com.psychology.psychology_backend.entity.Counselor;
import com.psychology.psychology_backend.entity.Review;
import com.psychology.psychology_backend.entity.User;
import com.psychology.psychology_backend.mapper.ReviewMapper;
import com.psychology.psychology_backend.service.AppointmentService;
import com.psychology.psychology_backend.service.ConsultationRecordService;
import com.psychology.psychology_backend.service.CounselorService;
import com.psychology.psychology_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/records")
public class ConsultationRecordController {

    @Autowired
    private ConsultationRecordService consultationRecordService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private ReviewMapper reviewMapper;

    // 获取咨询记录列表（咨询师看自己的，用户看自己的）
    @GetMapping
    public Result<Map<String, Object>> getRecords(
            @RequestAttribute(value = "userId", required = false) Long authUserId,
            @RequestAttribute(value = "userType", required = false) String userType,
            @RequestParam(required = false) Integer status) {

        List<Map<String, Object>> recordList = new ArrayList<>();

        if ("counselor".equals(userType) || "ROLE_ADMIN".equals(userType)) {
            if (authUserId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", recordList);
                result.put("total", 0);
                return Result.success(result);
            }

            // 1. 查出已完成的预约（status=2）
            LambdaQueryWrapper<Appointment> apptWrapper = new LambdaQueryWrapper<>();
            apptWrapper.eq(Appointment::getCounselorId, authUserId)
                       .eq(Appointment::getStatus, 2)
                       .orderByDesc(Appointment::getAppointmentTime);
            List<Appointment> completedAppts = appointmentService.list(apptWrapper);

            // 2. 查出已有的咨询记录
            LambdaQueryWrapper<ConsultationRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ConsultationRecord::getCounselorId, authUserId);
            if (status != null) {
                recordWrapper.eq(ConsultationRecord::getStatus, status);
            }
            List<ConsultationRecord> existingRecords = consultationRecordService.list(recordWrapper);

            // 3. 已建记录的 appointmentId 集合
            Set<Long> recordedApptIds = new HashSet<>();
            for (ConsultationRecord r : existingRecords) {
                if (r.getAppointmentId() != null) {
                    recordedApptIds.add(r.getAppointmentId());
                }
            }

            // 4. 先输出已有记录
            for (ConsultationRecord record : existingRecords) {
                recordList.add(buildRecordItem(record));
            }

            // 5. 已完成但未建记录的预约，生成占位草稿
            for (Appointment appt : completedAppts) {
                if (!recordedApptIds.contains(appt.getId())) {
                    // 根据 status 过滤：用户只看某一种时跳过
                    if (status != null && status == 1) continue; // 查已完成，草稿不展示

                    Map<String, Object> item = new HashMap<>();
                    item.put("id", null);
                    item.put("appointmentId", appt.getId());
                    item.put("userId", appt.getUserId());
                    item.put("counselorId", appt.getCounselorId());

                    User user = userService.getById(appt.getUserId());
                    item.put("userName", user != null ? user.getNickname() : "未知用户");

                    Counselor counselor = counselorService.getById(appt.getCounselorId());
                    item.put("counselorName", counselor != null ? counselor.getName() : "未知咨询师");
                    item.put("counselorAvatar", counselor != null ? counselor.getAvatar() : "");

                    String[] serviceTypes = {"", "电话咨询", "网络咨询", "门诊咨询"};
                    int st = appt.getServiceType() != null ? appt.getServiceType() : 0;
                    item.put("serviceType", st >= 1 && st <= 3 ? serviceTypes[st] : "未知");
                    item.put("date", appt.getAppointmentTime() != null
                            ? appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
                    item.put("time", appt.getAppointmentTime() != null
                            ? appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");

                    item.put("problem", appt.getProblem());
                    item.put("diagnosis", null);
                    item.put("suggestions", null);
                    item.put("duration", null);
                    item.put("fee", null);
                    item.put("status", "draft");
                    item.put("isPlaceholder", true);

                    recordList.add(item);
                }
            }
        } else {
            // 用户端：已完成预约 + 已有记录合并
            if (authUserId != null) {
                // 1. 查出已完成的预约
                LambdaQueryWrapper<Appointment> apptWrapper = new LambdaQueryWrapper<>();
                apptWrapper.eq(Appointment::getUserId, authUserId)
                           .eq(Appointment::getStatus, 2)
                           .orderByDesc(Appointment::getAppointmentTime);
                List<Appointment> completedAppts = appointmentService.list(apptWrapper);

                // 2. 查出已有记录
                LambdaQueryWrapper<ConsultationRecord> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(ConsultationRecord::getUserId, authUserId);
                if (status != null) {
                    recordWrapper.eq(ConsultationRecord::getStatus, status);
                }
                recordWrapper.orderByDesc(ConsultationRecord::getCreateTime);
                List<ConsultationRecord> existingRecords = consultationRecordService.list(recordWrapper);

                // 已建记录的 appointmentId 集合
                Set<Long> recordedApptIds = new HashSet<>();
                for (ConsultationRecord r : existingRecords) {
                    if (r.getAppointmentId() != null) {
                        recordedApptIds.add(r.getAppointmentId());
                    }
                }

                // 先输出已有记录
                for (ConsultationRecord record : existingRecords) {
                    recordList.add(buildRecordItem(record));
                }

                // 已完成但未建记录的预约，生成占位（仅全部和草稿Tab展示）
                if (status == null || status == 0) {
                for (Appointment appt : completedAppts) {
                    if (!recordedApptIds.contains(appt.getId())) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", null);
                        item.put("appointmentId", appt.getId());
                        item.put("userId", appt.getUserId());
                        item.put("counselorId", appt.getCounselorId());

                        User user = userService.getById(appt.getUserId());
                        item.put("userName", user != null ? user.getNickname() : "未知用户");

                        Counselor counselor = counselorService.getById(appt.getCounselorId());
                        item.put("counselorName", counselor != null ? counselor.getName() : "未知咨询师");
                        item.put("counselorAvatar", counselor != null ? counselor.getAvatar() : "");

                        String[] serviceTypes = {"", "电话咨询", "网络咨询", "门诊咨询"};
                        int st = appt.getServiceType() != null ? appt.getServiceType() : 0;
                        item.put("serviceType", st >= 1 && st <= 3 ? serviceTypes[st] : "未知");
                        item.put("date", appt.getAppointmentTime() != null
                                ? appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
                        item.put("time", appt.getAppointmentTime() != null
                                ? appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");

                        item.put("problem", appt.getProblem());
                        item.put("diagnosis", null);
                        item.put("suggestions", null);
                        item.put("fee", null);
                        item.put("status", "draft");
                        item.put("isPlaceholder", true);
                        item.put("hasReview", false);
                        item.put("createTime", appt.getCreateTime());

                        recordList.add(item);
                    }
                }
            }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", recordList);
        result.put("total", recordList.size());
        return Result.success(result);
    }

    private Map<String, Object> buildRecordItem(ConsultationRecord record) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", record.getId());
        item.put("appointmentId", record.getAppointmentId());
        item.put("userId", record.getUserId());
        item.put("counselorId", record.getCounselorId());
        item.put("createTime", record.getCreateTime());

        User user = userService.getById(record.getUserId());
        item.put("userName", user != null ? user.getNickname() : "未知用户");

        Counselor counselor = counselorService.getById(record.getCounselorId());
        item.put("counselorName", counselor != null ? counselor.getName() : "未知咨询师");
        item.put("counselorAvatar", counselor != null ? counselor.getAvatar() : "");

        Appointment appointment = appointmentService.getById(record.getAppointmentId());
        if (appointment != null) {
            String[] serviceTypes = {"", "电话咨询", "网络咨询", "门诊咨询"};
            int st = appointment.getServiceType() != null ? appointment.getServiceType() : 0;
            item.put("serviceType", st >= 1 && st <= 3 ? serviceTypes[st] : "未知");
            item.put("date", appointment.getAppointmentTime() != null
                    ? appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            item.put("time", appointment.getAppointmentTime() != null
                    ? appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
        }

        item.put("problem", record.getProblem());
        item.put("diagnosis", record.getDiagnosis());
        item.put("suggestions", record.getSuggestions());
        item.put("duration", record.getDuration());
        item.put("fee", record.getFee());
        item.put("status", record.getStatus() == 1 ? "completed" : "draft");
        item.put("isPlaceholder", false);

        // 查评价状态
        boolean hasReview = reviewMapper.selectCount(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, record.getUserId())
                .eq(Review::getCounselorId, record.getCounselorId())
                .eq(Review::getAppointmentId, record.getAppointmentId())) > 0;
        item.put("hasReview", hasReview);

        return item;
    }

    // 获取单个咨询记录详情
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getRecordDetail(@PathVariable Long id) {
        ConsultationRecord record = consultationRecordService.getById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("appointmentId", record.getAppointmentId());
        result.put("userId", record.getUserId());
        result.put("counselorId", record.getCounselorId());

        User user = userService.getById(record.getUserId());
        result.put("userName", user != null ? user.getNickname() : "未知用户");
        result.put("userAvatar", user != null ? user.getAvatar() : "");

        Counselor counselor = counselorService.getById(record.getCounselorId());
        result.put("counselorName", counselor != null ? counselor.getName() : "未知咨询师");
        result.put("counselorAvatar", counselor != null ? counselor.getAvatar() : "");

        Appointment appointment = appointmentService.getById(record.getAppointmentId());
        if (appointment != null) {
            String[] serviceTypes = {"", "电话咨询", "网络咨询", "门诊咨询"};
            result.put("serviceType", appointment.getServiceType() != null && appointment.getServiceType() >= 1 && appointment.getServiceType() <= 3
                    ? serviceTypes[appointment.getServiceType()] : "未知");
            result.put("appointmentTime", appointment.getAppointmentTime());
        }
        
        result.put("problem", record.getProblem());
        result.put("diagnosis", record.getDiagnosis());
        result.put("suggestions", record.getSuggestions());
        result.put("duration", record.getDuration());
        result.put("fee", record.getFee());
        result.put("status", record.getStatus());
        result.put("createTime", record.getCreateTime());

        boolean hasReview = reviewMapper.selectCount(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, record.getUserId())
                .eq(Review::getCounselorId, record.getCounselorId())
                .eq(Review::getAppointmentId, record.getAppointmentId())) > 0;
        result.put("hasReview", hasReview);

        return Result.success(result);
    }

    // 获取某咨询师的已完成案例（公开查看，用于咨询师详情页展示）
    @GetMapping("/counselor/{counselorId}")
    public Result<List<Map<String, Object>>> getCounselorCases(@PathVariable Long counselorId) {
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getCounselorId, counselorId)
               .eq(ConsultationRecord::getStatus, 1)
               .orderByDesc(ConsultationRecord::getCreateTime)
               .last("LIMIT 10");

        List<ConsultationRecord> records = consultationRecordService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ConsultationRecord record : records) {
            // 排除关联预约已取消的记录
            if (record.getAppointmentId() != null) {
                Appointment appointment = appointmentService.getById(record.getAppointmentId());
                if (appointment == null || appointment.getStatus() == 3) {
                    continue;
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("problem", record.getProblem());
            item.put("diagnosis", record.getDiagnosis());
            item.put("suggestions", record.getSuggestions());
            item.put("duration", record.getDuration());
            item.put("fee", record.getFee());
            item.put("createTime", record.getCreateTime());

            User user = userService.getById(record.getUserId());
            item.put("userName", user != null ? user.getNickname() : "匿名用户");

            result.add(item);
        }
        return Result.success(result);
    }

    // 保存/更新咨询记录
    @PostMapping
    public Result<String> saveRecord(@RequestBody ConsultationRecord record) {
        try {
            if (record.getUserId() == null || record.getCounselorId() == null) {
                return Result.error("用户ID和咨询师ID不能为空");
            }

            // 手动设置时间，避免自动填充失效
            LocalDateTime now = LocalDateTime.now();
            if (record.getId() == null) {
                record.setCreateTime(now);
                record.setUpdateTime(now);
                consultationRecordService.save(record);
            } else {
                record.setUpdateTime(now);
                consultationRecordService.updateById(record);
            }
            return Result.success("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存失败: " + e.getMessage());
        }
    }
}
