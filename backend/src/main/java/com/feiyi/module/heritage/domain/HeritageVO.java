package com.feiyi.module.heritage.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 非遗项目视图对象
 *
 * @author system
 */
@Data
@Schema(description = "非遗项目视图对象")
public class HeritageVO {

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String name;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 级别
     */
    @Schema(description = "级别")
    private String level;

    /**
     * 所属地区（申报地区）
     */
    @Schema(description = "申报地区")
    private String region;

    /**
     * 保护单位
     */
    @Schema(description = "保护单位")
    private String protectionUnit;

    /**
     * 公布时间
     */
    @Schema(description = "公布时间")
    private String publishTime;

    /**
     * 传承人
     */
    @Schema(description = "传承人")
    private String inheritor;

    /**
     * 简介
     */
    @Schema(description = "项目简介")
    private String description;

    /**
     * 历史渊源
     */
    @Schema(description = "历史渊源")
    private String history;

    /**
     * 封面图片
     */
    @Schema(description = "封面图片")
    private String coverImage;

    /**
     * 浏览次数（浏览量）
     */
    @Schema(description = "浏览量")
    private Integer viewCount;

    /**
     * 收藏数
     */
    @Schema(description = "收藏数")
    private Integer favoriteCount;

    /**
     * 是否有关联展品
     */
    @Schema(description = "是否有关联展品")
    private Boolean hasExhibit;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
