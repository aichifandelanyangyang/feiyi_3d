package com.feiyi.module.ai.domain;

import lombok.Data;

import java.util.List;

/**
 * AI聊天响应
 */
@Data
public class ChatResponse {

    private String reply;

    private List<String> sources;

    private List<ExhibitLink> exhibits;
}
