package com.feiyi.module.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.ai.dao.KnowledgeDao;
import com.feiyi.module.ai.domain.ChatRequest;
import com.feiyi.module.ai.domain.ChatResponse;
import com.feiyi.module.ai.domain.ExhibitLink;
import com.feiyi.module.ai.domain.KnowledgeEntity;
import com.feiyi.module.exhibition.dao.ExhibitDao;
import com.feiyi.module.exhibition.domain.ExhibitEntity;
import com.feiyi.module.heritage.dao.HeritageDao;
import com.feiyi.module.heritage.domain.HeritageEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * AI服务 - RAG知识检索 + LLM调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final KnowledgeDao knowledgeDao;
    private final HeritageDao heritageDao;
    private final ExhibitDao exhibitDao;
    private final AiConfig aiConfig;

    @Qualifier("aiStreamExecutor")
    private final Executor aiStreamExecutor;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * RAG检索结果封装
     */
    @Data
    public static class RagResult {
        private List<String> sources;
        private List<ExhibitLink> exhibits;
    }

    private static final String SYSTEM_PROMPT = """
            你是"非遗智识"——非遗3D数字化交互平台的AI智能知识助手。
            你专注于中国非物质文化遗产领域，能够回答关于非遗项目、传统技艺、文化传承等方面的问题。

            【重要】你必须始终使用简体中文（中国大陆规范汉字）回复所有内容，禁止使用繁体中文、港澳台用词或任何非规范汉字。

            请根据以下知识库中检索到的相关资料来回答用户的问题。
            如果知识库中有相关信息，请优先基于知识库内容回答并适当扩展。
            如果知识库中没有直接相关的信息，请基于你的通用知识回答，但保持回答与非遗文化相关。
            当用户询问你是谁时，请说明你是"非遗智识"AI助手，基于人工智能大模型与RAG知识库检索技术，接入了平台的非遗知识库与项目数据。

            ## 禁止事项

            - 不要在回复末尾添加任何展品推荐提示语（如"💡 平台展厅中有..."等）
            - 不要在回复中生成任何展品链接或展厅链接（如"[点击查看...](#)"等）
            - 展品推荐链接会由系统自动判断并展示，你只需专注于回答问题本身

            ## 回答格式要求

            请使用 Markdown 格式组织回答内容，使内容层次清晰、易于阅读：

            1. **标题使用**：主要观点可用 `## 二级标题` 或 `### 三级标题` 标注
            2. **列表使用**：列举多个要点时使用无序列表 `- 要点内容` 或有序列表 `1. 第一点`
            3. **重点强调**：重要词汇或关键信息使用 `**加粗**` 标记
            4. **适当分段**：不同内容之间空一行，保持视觉层次

            回答要求：专业但通俗易懂，语言优美，体现对传统文化的尊重与热爱。
            """;

    /**
     * AI对话
     */
    public ResponseDTO<ChatResponse> chat(ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseDTO.error("请输入您的问题");
        }

        if (aiConfig.getApiKey() == null || aiConfig.getApiKey().isBlank()) {
            return ResponseDTO.error("AI服务未配置，请联系管理员设置API密钥");
        }

        try {
            String context = "";
            List<String> sources = new ArrayList<>();
            List<ExhibitLink> exhibitLinks = new ArrayList<>();

            boolean useRag = aiConfig.getRagUrl() != null && !aiConfig.getRagUrl().isBlank();

            if (useRag) {
                // ===== 语义检索模式（Python ChromaDB）=====
                log.info("使用 RAG 语义检索: {}", request.getMessage());
                JSONArray ragResults = callRagSearch(request.getMessage());

                if (ragResults != null && !ragResults.isEmpty()) {
                    context = buildContextFromRag(ragResults);
                    sources = buildSourcesFromRag(ragResults);
                    exhibitLinks = buildExhibitLinksFromRag(ragResults);
                } else {
                    // RAG 服务无结果或不可用，回退到 MySQL LIKE
                    log.info("RAG 无结果，回退到 MySQL LIKE 检索");
                    useRag = false;
                }
            }
            if (!useRag) {
                // ===== 关键词检索模式（MySQL LIKE 回退）=====
                log.info("使用 MySQL LIKE 检索（RAG 服务未配置）");
                String topic = extractTopic(request.getMessage());

                List<KnowledgeEntity> knowledgeList = searchKnowledge(topic);
                List<HeritageEntity> heritageList = searchHeritage(topic);
                List<ExhibitEntity> exhibitList = searchExhibits(topic);
                // 展品找不到时，通过关联的非遗项目再查一次
                if (exhibitList.isEmpty() && !heritageList.isEmpty()) {
                    List<Long> heritageIds = heritageList.stream()
                            .map(HeritageEntity::getId).collect(Collectors.toList());
                    exhibitList = searchExhibitsByHeritageIds(heritageIds);
                }

                context = buildContext(knowledgeList, heritageList, exhibitList);
                sources = buildSources(knowledgeList, heritageList);
                exhibitLinks = buildExhibitLinks(exhibitList);
            }

            // 构建消息列表 -> 调用 LLM
            JSONArray messages = buildMessages(context, request);
            String reply = callLlm(messages);

            // 构建响应
            ChatResponse response = new ChatResponse();
            response.setReply(reply);
            response.setSources(sources);
            response.setExhibits(exhibitLinks);
            return ResponseDTO.succ(response);

        } catch (Exception e) {
            log.error("AI对话失败", e);
            return ResponseDTO.error("AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * AI对话 - SSE流式输出
     */
    public SseEmitter chatStream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2分钟超时

        aiStreamExecutor.execute(() -> {
            try {
                // 参数校验
                if (request.getMessage() == null || request.getMessage().isBlank()) {
                    emitter.send(SseEmitter.event().name("error").data("{\"message\":\"请输入您的问题\"}"));
                    emitter.complete();
                    return;
                }

                if (aiConfig.getApiKey() == null || aiConfig.getApiKey().isBlank()) {
                    emitter.send(SseEmitter.event().name("error").data("{\"message\":\"AI服务未配置\"}"));
                    emitter.complete();
                    return;
                }

                // RAG检索（同步，快速）
                String context = "";
                List<String> sources = new ArrayList<>();
                List<ExhibitLink> exhibitLinks = new ArrayList<>();

                boolean useRag = aiConfig.getRagUrl() != null && !aiConfig.getRagUrl().isBlank();

                if (useRag) {
                    log.info("SSE RAG 语义检索: {}", request.getMessage());
                    JSONArray ragResults = callRagSearch(request.getMessage());
                    if (ragResults != null && !ragResults.isEmpty()) {
                        context = buildContextFromRag(ragResults);
                        sources = buildSourcesFromRag(ragResults);
                        exhibitLinks = buildExhibitLinksFromRag(ragResults);
                    } else {
                        useRag = false;
                    }
                }
                if (!useRag) {
                    log.info("SSE MySQL LIKE 检索");
                    String topic = extractTopic(request.getMessage());
                    List<KnowledgeEntity> knowledgeList = searchKnowledge(topic);
                    List<HeritageEntity> heritageList = searchHeritage(topic);
                    List<ExhibitEntity> exhibitList = searchExhibits(topic);
                    if (exhibitList.isEmpty() && !heritageList.isEmpty()) {
                        List<Long> heritageIds = heritageList.stream()
                                .map(HeritageEntity::getId).collect(Collectors.toList());
                        exhibitList = searchExhibitsByHeritageIds(heritageIds);
                    }
                    context = buildContext(knowledgeList, heritageList, exhibitList);
                    sources = buildSources(knowledgeList, heritageList);
                    exhibitLinks = buildExhibitLinks(exhibitList);
                }

                // 发送 metadata 事件（sources + exhibits）
                RagResult metadata = new RagResult();
                metadata.setSources(sources);
                metadata.setExhibits(exhibitLinks);
                emitter.send(SseEmitter.event().name("metadata").data(JSONUtil.toJsonStr(metadata)));

                // 构建消息列表
                JSONArray messages = buildMessages(context, request);

                // 流式调用 LLM
                streamCallLlm(messages, emitter);

                // 发送 done 事件
                emitter.send(SseEmitter.event().name("done").data("[STREAM_END]"));
                emitter.complete();

            } catch (Exception e) {
                log.error("SSE AI对话失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("{\"message\":\"AI服务暂时不可用\"}"));
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            }
        });

        return emitter;
    }

    /**
     * 流式调用 LLM API (OpenAI兼容格式)
     */
    private void streamCallLlm(JSONArray messages, SseEmitter emitter) {
        try {
            JSONObject body = new JSONObject();
            body.set("model", aiConfig.getModel());
            body.set("messages", messages);
            body.set("stream", true);
            body.set("temperature", 0.7);
            body.set("max_tokens", 2048);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getApiUrl()))
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<java.util.stream.Stream<String>> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofLines()
            );

            if (response.statusCode() != 200) {
                log.error("LLM API返回错误状态: {}", response.statusCode());
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"message\":\"LLM服务返回错误\"}"));
                return;
            }

            // 解析 SSE 行
            response.body().forEach(line -> {
                try {
                    if (line == null || line.isBlank()) return;
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) return;

                        JSONObject json = JSONUtil.parseObj(data);
                        JSONArray choices = json.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject delta = choice.getJSONObject("delta");
                            if (delta != null) {
                                String content = delta.getStr("content");
                                if (content != null && !content.isEmpty()) {
                                    JSONObject chunk = new JSONObject();
                                    chunk.set("text", content);
                                    emitter.send(SseEmitter.event().name("content").data(chunk.toString()));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析SSE行异常: {}", line, e);
                }
            });

        } catch (Exception e) {
            log.error("流式调用LLM失败", e);
            throw new RuntimeException("LLM调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用 Python RAG 语义检索服务
     */
    private JSONArray callRagSearch(String query) {
        try {
            JSONObject body = new JSONObject();
            body.set("query", query);
            body.set("top_k", 8);

            String responseStr = HttpRequest.post(aiConfig.getRagUrl() + "/api/rag/search")
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(10000)
                    .execute()
                    .body();

            JSONObject res = JSONUtil.parseObj(responseStr);
            return res.getJSONArray("results");
        } catch (Exception e) {
            log.warn("RAG 服务调用失败，回退到 MySQL LIKE: {}", e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * 从 RAG 检索结果构建 LLM 上下文
     */
    private String buildContextFromRag(JSONArray results) {
        if (results == null || results.isEmpty()) {
            return "（知识库中暂无直接相关的资料，请基于通用知识回答）\n";
        }

        StringBuilder sbKnowledge = new StringBuilder();
        StringBuilder sbHeritage = new StringBuilder();
        StringBuilder sbExhibit = new StringBuilder();

        for (int i = 0; i < results.size(); i++) {
            JSONObject item = results.getJSONObject(i);
            String sourceType = item.getStr("source_type", "");
            String document = item.getStr("document", "");
            JSONObject meta = item.getJSONObject("metadata");

            switch (sourceType) {
                case "knowledge" -> {
                    String title = meta != null ? meta.getStr("title", "") : "";
                    sbKnowledge.append("▸ ").append(title).append("：").append(document).append("\n\n");
                }
                case "heritage" -> {
                    String name = meta != null ? meta.getStr("name", "") : "";
                    sbHeritage.append("▸ ").append(name).append("：").append(document).append("\n\n");
                }
                case "exhibit" -> {
                    double dist = item.getDouble("distance", 2.0);
                    if (dist < 1.5) {
                        String name = meta != null ? meta.getStr("name", "") : "";
                        sbExhibit.append("▸ ").append(name);
                        sbExhibit.append("：").append(document).append("\n\n");
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        if (!sbKnowledge.isEmpty()) sb.append("【知识库资料】\n").append(sbKnowledge);
        if (!sbHeritage.isEmpty()) sb.append("【非遗项目资料】\n").append(sbHeritage);
        if (!sbExhibit.isEmpty()) sb.append("【平台展品资料】\n").append(sbExhibit);

        if (sb.isEmpty()) {
            sb.append("（知识库中暂无直接相关的资料，请基于通用知识回答）\n");
        }
        return sb.toString();
    }

    /**
     * 从 RAG 检索结果构建引用来源
     */
    private List<String> buildSourcesFromRag(JSONArray results) {
        List<String> sources = new ArrayList<>();
        if (results == null) return sources;
        for (int i = 0; i < results.size(); i++) {
            JSONObject item = results.getJSONObject(i);
            JSONObject meta = item.getJSONObject("metadata");
            if (meta != null) {
                String name = meta.getStr("name", meta.getStr("title", ""));
                if (!name.isBlank() && !sources.contains(name)) {
                    sources.add(name);
                }
            }
        }
        return sources;
    }

    /**
     * 从 RAG 检索结果构建展品链接
     */
    private List<ExhibitLink> buildExhibitLinksFromRag(JSONArray results) {
        List<ExhibitLink> links = new ArrayList<>();
        if (results == null) return links;
        for (int i = 0; i < results.size(); i++) {
            JSONObject item = results.getJSONObject(i);
            if (!"exhibit".equals(item.getStr("source_type"))) continue;
            // 语义距离在一定范围内即认为相关，放宽匹配条件
            double distance = item.getDouble("distance", 2.0);
            if (distance >= 1.5) continue;
            JSONObject meta = item.getJSONObject("metadata");
            if (meta == null) continue;

            ExhibitLink link = new ExhibitLink();
            link.setId(meta.getLong("source_id"));
            link.setName(meta.getStr("name", ""));
            link.setImage(meta.getStr("image", ""));
            link.setExhibitionId(meta.getLong("exhibition_id"));
            links.add(link);
        }
        return links;
    }

    /**
     * 提取关键词 - 去除常见问句词汇
     */
    private String extractTopic(String message) {
        return message.replaceAll("(请问|请|你好|你能|可以|帮我|介绍一下|介绍|什么是|什么|告诉我|讲讲|说说|关于|有哪些|怎么样|怎么|如何|吗|呢|吧|的|了|啊|哦|？|\\?|！|!|。|，|,)", "").trim();
    }

    /**
     * 检索知识库
     */
    private List<KnowledgeEntity> searchKnowledge(String topic) {
        if (topic.isBlank()) return List.of();
        LambdaQueryWrapper<KnowledgeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeEntity::getDeletedFlag, 0)
                .and(w -> w.like(KnowledgeEntity::getTitle, topic)
                        .or().like(KnowledgeEntity::getContent, topic))
                .last("LIMIT 5");
        return knowledgeDao.selectList(wrapper);
    }

    /**
     * 检索非遗项目数据
     */
    private List<HeritageEntity> searchHeritage(String topic) {
        if (topic.isBlank()) return List.of();
        LambdaQueryWrapper<HeritageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeritageEntity::getDeletedFlag, 0)
                .and(w -> w.like(HeritageEntity::getName, topic)
                        .or().like(HeritageEntity::getDescription, topic)
                        .or().like(HeritageEntity::getHistory, topic))
                .last("LIMIT 5");
        return heritageDao.selectList(wrapper);
    }

    /**
     * 检索平台展品（名称 / 分类 / 描述 多字段匹配）
     */
    private List<ExhibitEntity> searchExhibits(String topic) {
        if (topic.isBlank()) return List.of();
        LambdaQueryWrapper<ExhibitEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExhibitEntity::getDeletedFlag, 0)
                .and(w -> w.like(ExhibitEntity::getName, topic)
                        .or().like(ExhibitEntity::getCategory, topic)
                        .or().like(ExhibitEntity::getDescription, topic))
                .last("LIMIT 3");
        return exhibitDao.selectList(wrapper);
    }

    /**
     * 通过非遗项目ID列表查找关联展品
     */
    private List<ExhibitEntity> searchExhibitsByHeritageIds(List<Long> heritageIds) {
        if (heritageIds.isEmpty()) return List.of();
        LambdaQueryWrapper<ExhibitEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExhibitEntity::getDeletedFlag, 0)
                .in(ExhibitEntity::getHeritageId, heritageIds)
                .last("LIMIT 3");
        return exhibitDao.selectList(wrapper);
    }

    /**
     * 构建展品链接列表
     */
    private List<ExhibitLink> buildExhibitLinks(List<ExhibitEntity> exhibitList) {
        List<ExhibitLink> links = new ArrayList<>();
        for (ExhibitEntity e : exhibitList) {
            ExhibitLink link = new ExhibitLink();
            link.setId(e.getId());
            link.setName(e.getName());
            link.setImage(e.getImage());
            link.setExhibitionId(e.getExhibitionId());
            links.add(link);
        }
        return links;
    }

    /**
     * 构建RAG上下文
     */
    private String buildContext(List<KnowledgeEntity> knowledgeList, List<HeritageEntity> heritageList, List<ExhibitEntity> exhibitList) {
        StringBuilder sb = new StringBuilder();
        if (!knowledgeList.isEmpty()) {
            sb.append("【知识库资料】\n");
            for (KnowledgeEntity k : knowledgeList) {
                sb.append("▸ ").append(k.getTitle()).append("：").append(k.getContent()).append("\n\n");
            }
        }
        if (!heritageList.isEmpty()) {
            sb.append("【非遗项目资料】\n");
            for (HeritageEntity h : heritageList) {
                sb.append("▸ ").append(h.getName());
                if (h.getLevel() != null) sb.append("（").append(h.getLevel()).append("）");
                if (h.getRegion() != null) sb.append("，产地：").append(h.getRegion());
                if (h.getInheritor() != null) sb.append("，传承人：").append(h.getInheritor());
                sb.append("\n");
                if (h.getDescription() != null) sb.append("  简介：").append(h.getDescription()).append("\n");
                if (h.getHistory() != null) sb.append("  历史渊源：").append(h.getHistory()).append("\n");
                sb.append("\n");
            }
        }
        if (!exhibitList.isEmpty()) {
            sb.append("【平台展品资料】\n");
            for (ExhibitEntity e : exhibitList) {
                sb.append("▸ ").append(e.getName());
                if (e.getCategory() != null) sb.append("（").append(e.getCategory()).append("）");
                if (e.getEra() != null) sb.append("，年代：").append(e.getEra());
                if (e.getOrigin() != null) sb.append("，产地：").append(e.getOrigin());
                sb.append("\n");
                if (e.getDescription() != null) sb.append("  简介：").append(e.getDescription()).append("\n");
                if (e.getHistory() != null) sb.append("  历史：").append(e.getHistory()).append("\n");
                if (e.getCraft() != null) sb.append("  工艺：").append(e.getCraft()).append("\n");
                sb.append("\n");
            }
        }
        if (sb.isEmpty()) {
            sb.append("（知识库中暂无直接相关的资料，请基于通用知识回答）\n");
        }
        return sb.toString();
    }

    /**
     * 构建LLM消息列表
     */
    private JSONArray buildMessages(String context, ChatRequest request) {
        JSONArray messages = new JSONArray();

        // system prompt + 检索上下文
        JSONObject system = new JSONObject();
        system.set("role", "system");
        system.set("content", SYSTEM_PROMPT + "\n\n" + context);
        messages.add(system);

        // 历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                JSONObject m = new JSONObject();
                m.set("role", msg.getRole());
                m.set("content", msg.getContent());
                messages.add(m);
            }
        }

        // 当前用户消息
        JSONObject user = new JSONObject();
        user.set("role", "user");
        user.set("content", request.getMessage());
        messages.add(user);

        return messages;
    }

    /**
     * 调用LLM API (OpenAI兼容格式)
     */
    private String callLlm(JSONArray messages) {
        JSONObject body = new JSONObject();
        body.set("model", aiConfig.getModel());
        body.set("messages", messages);
        body.set("temperature", 0.7);
        body.set("max_tokens", 2048);

        String responseStr = HttpRequest.post(aiConfig.getApiUrl())
                .header("Authorization", "Bearer " + aiConfig.getApiKey())
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(60000)
                .execute()
                .body();

        log.info("LLM API响应: {}", responseStr != null && responseStr.length() > 500 ? responseStr.substring(0, 500) + "..." : responseStr);

        if (responseStr == null || responseStr.isBlank()) {
            log.warn("LLM返回空响应");
            return "抱歉，AI助手暂时无法回答，请稍后再试。";
        }

        JSONObject responseJson = JSONUtil.parseObj(responseStr);

        // 检查是否有错误信息
        if (responseJson.containsKey("error")) {
            String errorMsg = responseJson.getJSONObject("error").getStr("message", "未知错误");
            log.error("LLM API错误: {}", errorMsg);
            return "AI服务暂时不可用：" + errorMsg;
        }

        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            return choices.getJSONObject(0).getJSONObject("message").getStr("content");
        }

        log.warn("LLM返回异常: {}", responseStr);
        return "抱歉，AI助手暂时无法回答，请稍后再试。";
    }

    /**
     * 构建引用来源列表
     */
    private List<String> buildSources(List<KnowledgeEntity> knowledgeList, List<HeritageEntity> heritageList) {
        List<String> sources = new ArrayList<>();
        for (KnowledgeEntity k : knowledgeList) {
            sources.add(k.getTitle());
        }
        for (HeritageEntity h : heritageList) {
            sources.add(h.getName());
        }
        return sources;
    }

    // ============ 知识库管理 ============

    /**
     * 知识库列表（支持按标题搜索）
     */
    public ResponseDTO<List<KnowledgeEntity>> listKnowledge(String title) {
        LambdaQueryWrapper<KnowledgeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeEntity::getDeletedFlag, 0);
        if (title != null && !title.isBlank()) {
            wrapper.like(KnowledgeEntity::getTitle, title);
        }
        wrapper.orderByDesc(KnowledgeEntity::getCreateTime);
        return ResponseDTO.succ(knowledgeDao.selectList(wrapper));
    }

    /**
     * 添加知识
     */
    public ResponseDTO<Void> addKnowledge(KnowledgeEntity entity) {
        entity.setDeletedFlag(0);
        knowledgeDao.insert(entity);
        return ResponseDTO.succ();
    }

    /**
     * 更新知识
     */
    public ResponseDTO<Void> updateKnowledge(Long id, KnowledgeEntity entity) {
        entity.setId(id);
        knowledgeDao.updateById(entity);
        return ResponseDTO.succ();
    }

    /**
     * 删除知识（逻辑删除）
     */
    public ResponseDTO<Void> deleteKnowledge(Long id) {
        // 使用 MyBatis-Plus 逻辑删除
        knowledgeDao.deleteById(id);
        return ResponseDTO.succ();
    }
}
