package com.feiyi.module.ai.domain;

import lombok.Data;

import java.util.List;

/**
 * AI聊天请求
 */
@Data
public class ChatRequest {

    private String message;

    private List<ChatMessage> history;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
