package com.feiyi.module.community.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 社区发帖DTO
 *
 * @author system
 */
@Data
@Schema(description = "发帖请求")
public class CommunityPostDTO {

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容")
    @NotBlank(message = "内容不能为空")
    private String content;

    @Schema(description = "图片（多张逗号分隔）")
    private String images;
}
