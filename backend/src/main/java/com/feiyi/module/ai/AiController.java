package com.feiyi.module.ai;

import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.ai.domain.ChatRequest;
import com.feiyi.module.ai.domain.ChatResponse;
import com.feiyi.module.ai.domain.KnowledgeEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI知识助手控制器
 */
@Tag(name = "AI知识助手")
@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    // ============ 公开接口 ============

    @Operation(summary = "AI对话")
    @PostMapping("/ai/chat")
    public ResponseDTO<ChatResponse> chat(@RequestBody ChatRequest request) {
        return aiService.chat(request);
    }

    // ============ 管理接口 ============

    @Operation(summary = "知识库列表")
    @GetMapping("/admin/knowledge/list")
    public ResponseDTO<List<KnowledgeEntity>> listKnowledge() {
        return aiService.listKnowledge();
    }

    @Operation(summary = "添加知识")
    @PostMapping("/admin/knowledge/add")
    public ResponseDTO<Void> addKnowledge(@RequestBody KnowledgeEntity entity) {
        return aiService.addKnowledge(entity);
    }

    @Operation(summary = "更新知识")
    @PostMapping("/admin/knowledge/update/{id}")
    public ResponseDTO<Void> updateKnowledge(@PathVariable Long id, @RequestBody KnowledgeEntity entity) {
        return aiService.updateKnowledge(id, entity);
    }

    @Operation(summary = "删除知识")
    @PostMapping("/admin/knowledge/delete/{id}")
    public ResponseDTO<Void> deleteKnowledge(@PathVariable Long id) {
        return aiService.deleteKnowledge(id);
    }
}
