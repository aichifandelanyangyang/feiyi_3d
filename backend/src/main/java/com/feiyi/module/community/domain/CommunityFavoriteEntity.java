package com.feiyi.module.community.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子收藏实体
 *
 * @author system
 */
@Data
@TableName("t_community_favorite")
public class CommunityFavoriteEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 帖子ID
     */
    private Long postId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}