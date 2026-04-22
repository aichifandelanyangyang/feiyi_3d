package com.feiyi.module.user.domain;

import com.feiyi.common.domain.PageParamDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询参数")
public class UserQueryDTO extends PageParamDTO {

    @Schema(description = "关键词(用户名/真实姓名)")
    private String keyword;

    @Schema(description = "角色类型")
    private Integer roleType;

    @Schema(description = "状态")
    private Integer status;
}
