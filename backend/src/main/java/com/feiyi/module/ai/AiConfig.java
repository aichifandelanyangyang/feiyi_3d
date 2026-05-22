package com.feiyi.module.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AI配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /**
     * LLM API地址 (OpenAI兼容格式)
     */
    private String apiUrl = "https://api.deepseek.com/chat/completions";

    /**
     * API密钥
     */
    private String apiKey = "";

    /**
     * 模型名称
     */
    private String model = "deepseek-chat";

    /**
     * RAG语义检索服务地址（Python FastAPI + ChromaDB）
     * 为空则回退到 MySQL LIKE 检索
     */
    private String ragUrl = "";

    /**
     * SSE流式响应线程池
     */
    @Bean("aiStreamExecutor")
    public Executor aiStreamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-stream-");
        executor.initialize();
        return executor;
    }
}
