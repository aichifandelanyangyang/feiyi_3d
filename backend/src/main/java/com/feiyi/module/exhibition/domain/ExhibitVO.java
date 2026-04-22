package com.feiyi.module.exhibition.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 展品视图对象
 *
 * @author system
 */
@Data
@Schema(description = "展品视图对象")
public class ExhibitVO {

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 展厅ID
     */
    @Schema(description = "展厅ID")
    private Long exhibitionId;

    /**
     * 关联非遗项目ID
     */
    @Schema(description = "关联非遗项目ID")
    private Long heritageId;

    /**
     * 非遗项目名称
     */
    @Schema(description = "非遗项目名称")
    private String heritageName;

    /**
     * 展品名称
     */
    @Schema(description = "展品名称")
    private String name;

    /**
     * 展品描述
     */
    @Schema(description = "展品描述")
    private String description;

    /**
     * 展品图片
     */
    @Schema(description = "展品图片")
    private String image;

    /**
     * 3D模型路径
     */
    @Schema(description = "3D模型路径")
    private String modelPath;

    /**
     * 在展厅中的X坐标
     */
    @Schema(description = "X坐标")
    private BigDecimal positionX;

    /**
     * 在展厅中的Y坐标
     */
    @Schema(description = "Y坐标")
    private BigDecimal positionY;

    /**
     * 在展厅中的Z坐标
     */
    @Schema(description = "Z坐标")
    private BigDecimal positionZ;

    /**
     * 展品类别
     */
    @Schema(description = "展品类别")
    private String category;

    /**
     * 年代
     */
    @Schema(description = "年代")
    private String era;

    /**
     * 产地
     */
    @Schema(description = "产地")
    private String origin;

    /**
     * 材质
     */
    @Schema(description = "材质")
    private String material;

    /**
     * 历史背景
     */
    @Schema(description = "历史背景")
    private String history;

    /**
     * 工艺特点
     */
    @Schema(description = "工艺特点")
    private String craft;

    /**
     * 文化价值
     */
    @Schema(description = "文化价值")
    private String culturalValue;
}
