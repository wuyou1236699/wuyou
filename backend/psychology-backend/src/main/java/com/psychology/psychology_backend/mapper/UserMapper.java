package com.psychology.psychology_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.psychology_backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 统计用户总数
    @Select("SELECT COUNT(*) FROM user")
    Long countTotalUsers();
}