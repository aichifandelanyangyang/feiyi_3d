package com.feiyi.module.community.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子VO
 *
 * @author system
 */
@Data
@Schema(description = "社区帖子视图")
public class CommunityPostVO {

    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片")
    private String images;

    @Schema(description = "审核状态 0待审核 1已通过 2已拒绝")
    private Integer status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞次数")
    private Integer likeCount;

    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
