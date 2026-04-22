package com.feiyi.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页参数基类
 *
 * @author system
 */
@Data
@Schema(description = "分页参数")
public class PageParamDTO {

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小值为1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小值为1")
    @Max(value = 100, message = "每页大小最大值为100")
    private Integer pageSize = 10;
}
