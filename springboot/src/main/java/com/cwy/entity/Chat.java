package com.cwy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tbl_chathis")
public class Chat {
	@TableId(type = IdType.AUTO)
	private Long id;
	private String input;
	private String output;
}
