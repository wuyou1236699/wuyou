package com.psychology.psychology_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.psychology_backend.entity.Counselor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CounselorMapper extends BaseMapper<Counselor> {

    @Select("SELECT COUNT(*) FROM counselor WHERE status = 1")
    Long countActiveCounselors();

    @Update("ALTER TABLE counselor AUTO_INCREMENT = #{autoIncrement}")
    void resetAutoIncrement(Long autoIncrement);
}