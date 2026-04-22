package com.feiyi.module.community;

import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.community.domain.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 社区控制器
 *
 * @author system
 */
@Tag(name = "社区接口")
@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // ===== 前台接口 =====

    @Operation(summary = "发帖")
    @PostMapping("/community/post")
    public ResponseDTO<Long> createPost(@RequestHeader("Authorization") String authHeader,
                                         @Valid @RequestBody CommunityPostDTO dto) {
        return communityService.createPost(authHeader, dto);
    }

    @Operation(summary = "帖子列表（已审核通过）")
    @GetMapping("/community/list")
    public ResponseDTO<PageResultDTO<CommunityPostVO>> list(CommunityQueryDTO query) {
        return communityService.listApproved(query);
    }

    @Operation(summary = "帖子详情")
    @GetMapping("/community/get/{id}")
    public ResponseDTO<CommunityPostVO> getDetail(@PathVariable Long id) {
        return communityService.getDetail(id);
    }

    @Operation(summary = "我的帖子")
    @GetMapping("/community/my")
    public ResponseDTO<PageResultDTO<CommunityPostVO>> myPosts(
            @RequestHeader("Authorization") String authHeader, CommunityQueryDTO query) {
        return communityService.myPosts(authHeader, query);
    }

    // ===== 后台管理接口 =====

    @Operation(summary = "后台帖子列表")
    @GetMapping("/admin/community/list")
    public ResponseDTO<PageResultDTO<CommunityPostVO>> adminList(CommunityQueryDTO query) {
        return communityService.adminList(query);
    }

    @Operation(summary = "审核通过")
    @PostMapping("/admin/community/approve/{id}")
    public ResponseDTO<Boolean> approve(@PathVariable Long id) {
        return communityService.approve(id);
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/admin/community/reject/{id}")
    public ResponseDTO<Boolean> reject(@PathVariable Long id, @RequestParam String reason) {
        return communityService.reject(id, reason);
    }

    @Operation(summary = "删除帖子")
    @PostMapping("/admin/community/delete/{id}")
    public ResponseDTO<Boolean> delete(@PathVariable Long id) {
        return communityService.delete(id);
    }
}
