package com.psychology.psychology_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.psychology_backend.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT " +
            "  cm1.user_id AS userId, " +
            "  u.nickname AS userName, " +
            "  u.avatar AS userAvatar, " +
            "  cm1.appointment_id AS appointmentId, " +
            "  a.appointment_time AS appointmentTime, " +
            "  a.problem AS problem, " +
            "  (SELECT cm2.content FROM chat_message cm2 " +
            "   WHERE cm2.user_id = cm1.user_id AND cm2.counselor_id = #{counselorId} " +
            "   AND (cm2.appointment_id IS NULL) = (cm1.appointment_id IS NULL) " +
            "   AND (cm2.appointment_id = cm1.appointment_id OR (cm2.appointment_id IS NULL AND cm1.appointment_id IS NULL)) " +
            "   AND cm2.deleted_by_counselor = 0 " +
            "   ORDER BY cm2.create_time DESC LIMIT 1) AS lastMessage, " +
            "  MAX(cm1.create_time) AS lastTime, " +
            "  (SELECT COUNT(*) FROM chat_message cm3 " +
            "   WHERE cm3.user_id = cm1.user_id AND cm3.counselor_id = #{counselorId} " +
            "   AND (cm3.appointment_id IS NULL) = (cm1.appointment_id IS NULL) " +
            "   AND (cm3.appointment_id = cm1.appointment_id OR (cm3.appointment_id IS NULL AND cm1.appointment_id IS NULL)) " +
            "   AND cm3.sender = 'user' AND cm3.is_read = 0 AND cm3.deleted_by_counselor = 0) AS unreadCount " +
            "FROM chat_message cm1 " +
            "JOIN user u ON u.id = cm1.user_id " +
            "LEFT JOIN appointment a ON a.id = cm1.appointment_id " +
            "WHERE cm1.counselor_id = #{counselorId} AND cm1.deleted_by_counselor = 0 " +
            "GROUP BY cm1.user_id, cm1.appointment_id IS NULL, cm1.appointment_id " +
            "ORDER BY lastTime DESC")
    List<Map<String, Object>> getChatList(@Param("counselorId") Long counselorId);

    @Select("<script>" +
            "SELECT id, user_id, counselor_id, appointment_id, sender, content, create_time " +
            "FROM chat_message " +
            "WHERE user_id = #{userId} AND counselor_id = #{counselorId} " +
            "<if test='appointmentId != null and appointmentId > 0'>" +
            "  AND appointment_id = #{appointmentId} " +
            "</if>" +
            "<if test='appointmentId != null and appointmentId == 0'>" +
            "  AND appointment_id IS NULL " +
            "</if>" +
            "AND ${deletedBy} = 0 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<ChatMessage> getMessagesPage(@Param("userId") Long userId,
                                      @Param("counselorId") Long counselorId,
                                      @Param("appointmentId") Long appointmentId,
                                      @Param("deletedBy") String deletedBy,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    // 用户端消息列表：按(咨询师, 预约)分组，直接对话和预约对话分开显示
    @Select("SELECT " +
            "  cm1.counselor_id AS counselorId, " +
            "  c.name AS counselorName, " +
            "  c.avatar AS counselorAvatar, " +
            "  cm1.appointment_id AS appointmentId, " +
            "  a.appointment_time AS appointmentTime, " +
            "  (SELECT cm2.content FROM chat_message cm2 " +
            "   WHERE cm2.user_id = #{userId} AND cm2.counselor_id = cm1.counselor_id " +
            "   AND (cm2.appointment_id IS NULL) = (cm1.appointment_id IS NULL) " +
            "   AND (cm2.appointment_id = cm1.appointment_id OR (cm2.appointment_id IS NULL AND cm1.appointment_id IS NULL)) " +
            "   AND cm2.deleted_by_user = 0 " +
            "   ORDER BY cm2.create_time DESC LIMIT 1) AS lastMessage, " +
            "  MAX(cm1.create_time) AS lastTime, " +
            "  (SELECT COUNT(*) FROM chat_message cm3 " +
            "   WHERE cm3.user_id = #{userId} AND cm3.counselor_id = cm1.counselor_id " +
            "   AND (cm3.appointment_id IS NULL) = (cm1.appointment_id IS NULL) " +
            "   AND (cm3.appointment_id = cm1.appointment_id OR (cm3.appointment_id IS NULL AND cm1.appointment_id IS NULL)) " +
            "   AND cm3.sender = 'counselor' AND cm3.is_read = 0 AND cm3.deleted_by_user = 0) AS unreadCount " +
            "FROM chat_message cm1 " +
            "JOIN counselor c ON c.id = cm1.counselor_id " +
            "LEFT JOIN appointment a ON a.id = cm1.appointment_id " +
            "WHERE cm1.user_id = #{userId} AND cm1.deleted_by_user = 0 " +
            "GROUP BY cm1.counselor_id, cm1.appointment_id IS NULL, cm1.appointment_id " +
            "ORDER BY lastTime DESC")
    List<Map<String, Object>> getUserChatList(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT COUNT(*) FROM chat_message " +
            "WHERE user_id = #{userId} AND counselor_id = #{counselorId} " +
            "<if test='appointmentId != null and appointmentId > 0'>" +
            "  AND appointment_id = #{appointmentId} " +
            "</if>" +
            "<if test='appointmentId != null and appointmentId == 0'>" +
            "  AND appointment_id IS NULL " +
            "</if>" +
            "AND ${deletedBy} = 0" +
            "</script>")
    int countMessages(@Param("userId") Long userId,
                      @Param("counselorId") Long counselorId,
                      @Param("appointmentId") Long appointmentId,
                      @Param("deletedBy") String deletedBy);
}
