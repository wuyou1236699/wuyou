package com.psychology.psychology_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.psychology_backend.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}