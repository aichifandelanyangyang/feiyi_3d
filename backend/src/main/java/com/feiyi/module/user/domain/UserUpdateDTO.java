package com.feiyi.module.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户更新DTO
 *
 * @author system
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateDTO {

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色类型 1管理员 3普通用户")
    private Integer roleType;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}
