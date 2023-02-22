package com.cwy.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cwy.entity.Chat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatDao extends BaseMapper<Chat> {

}
