package com.feiyi.module.community.domain;

import com.feiyi.common.domain.PageParamDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区帖子查询DTO
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "社区帖子查询参数")
public class CommunityQueryDTO extends PageParamDTO {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "审核状态 0待审核 1已通过 2已拒绝")
    private Integer status;

    @Schema(description = "用户ID")
    private Long userId;
}
