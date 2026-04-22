package com.feiyi.module.heritage.domain;

import com.feiyi.common.domain.PageParamDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 非遗项目查询参数
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "非遗项目查询参数")
public class HeritageQueryDTO extends PageParamDTO {

    /**
     * 关键词
     */
    @Schema(description = "关键词")
    private String keyword;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 级别
     */
    @Schema(description = "级别")
    private String level;

    /**
     * 地区
     */
    @Schema(description = "地区")
    private String region;
}
