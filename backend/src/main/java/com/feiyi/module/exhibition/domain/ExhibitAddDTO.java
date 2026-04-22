package com.feiyi.module.exhibition.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 展品新增/更新DTO
 *
 * @author system
 */
@Data
@Schema(description = "展品新增请求")
public class ExhibitAddDTO {

    @Schema(description = "展厅ID", required = true)
    @NotNull(message = "展厅ID不能为空")
    private Long exhibitionId;

    @Schema(description = "关联非遗项目ID")
    private Long heritageId;

    @Schema(description = "展品名称", required = true)
    @NotBlank(message = "展品名称不能为空")
    private String name;

    @Schema(description = "展品描述")
    private String description;

    @Schema(description = "展品图片")
    private String image;

    @Schema(description = "3D模型路径")
    private String modelPath;

    @Schema(description = "X坐标")
    private BigDecimal positionX;

    @Schema(description = "Y坐标")
    private BigDecimal positionY;

    @Schema(description = "Z坐标")
    private BigDecimal positionZ;

    @Schema(description = "展品类别")
    private String category;

    @Schema(description = "年代")
    private String era;

    @Schema(description = "产地")
    private String origin;

    @Schema(description = "材质")
    private String material;

    @Schema(description = "历史背景")
    private String history;

    @Schema(description = "工艺特点")
    private String craft;

    @Schema(description = "文化价值")
    private String culturalValue;

    @Schema(description = "排序")
    private Integer sort;
}
