package com.cwy.service;

import com.cwy.entity.Chat;

import java.util.List;

public interface ChatService {
	public Chat getByFuzzyQueryInput(String input);
}
