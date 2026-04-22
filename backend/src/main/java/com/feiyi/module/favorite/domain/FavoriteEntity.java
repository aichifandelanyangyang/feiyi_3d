package com.feiyi.module.favorite.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体
 *
 * @author system
 */
@Data
@TableName("t_favorite")
public class FavoriteEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 非遗项目ID
     */
    private Long heritageId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
