package com.feiyi.module.community.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子点赞实体
 *
 * @author system
 */
@Data
@TableName("t_community_like")
public class CommunityLikeEntity {

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