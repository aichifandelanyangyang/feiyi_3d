package com.feiyi.module.exhibition.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 展厅视图对象
 *
 * @author system
 */
@Data
@Schema(description = "展厅视图对象")
public class ExhibitionVO {

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 展厅名称
     */
    @Schema(description = "展厅名称")
    private String name;

    /**
     * 展厅描述
     */
    @Schema(description = "展厅描述")
    private String description;

    /**
     * 展厅封面图片
     */
    @Schema(description = "展厅封面图片")
    private String coverImage;

    /**
     * 3D模型文件路径
     */
    @Schema(description = "3D模型文件路径")
    private String modelPath;

    /**
     * 访问量
     */
    @Schema(description = "访问量")
    private Integer visitCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
