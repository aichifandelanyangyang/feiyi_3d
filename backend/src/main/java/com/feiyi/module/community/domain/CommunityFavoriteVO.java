package com.feiyi.module.community.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子收藏/点赞VO
 *
 * @author system
 */
@Data
@Schema(description = "社区帖子收藏视图")
public class CommunityFavoriteVO {

    @Schema(description = "收藏/点赞ID")
    private Long id;

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "帖子标题")
    private String postTitle;

    @Schema(description = "帖子内容")
    private String postContent;

    @Schema(description = "帖子图片")
    private String postImages;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者名称")
    private String authorName;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞次数")
    private Integer likeCount;

    @Schema(description = "收藏/点赞时间")
    private LocalDateTime createTime;
}