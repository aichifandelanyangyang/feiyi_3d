package com.feiyi.module.favorite.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏VO（含非遗项目信息）
 *
 * @author system
 */
@Data
@Schema(description = "收藏视图")
public class FavoriteVO {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "非遗项目ID")
    private Long heritageId;

    @Schema(description = "项目名称")
    private String heritageName;

    @Schema(description = "项目封面图")
    private String coverImage;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "级别")
    private String level;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}
