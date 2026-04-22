package com.feiyi.module.exhibition.domain;

import com.feiyi.common.domain.PageParamDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 展品查询DTO
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "展品查询参数")
public class ExhibitQueryDTO extends PageParamDTO {

    @Schema(description = "展厅ID")
    private Long exhibitionId;

    @Schema(description = "关联非遗项目ID")
    private Long heritageId;

    @Schema(description = "展品名称关键词")
    private String keyword;

    @Schema(description = "描述关键词")
    private String description;

    @Schema(description = "类别")
    private String category;
}
