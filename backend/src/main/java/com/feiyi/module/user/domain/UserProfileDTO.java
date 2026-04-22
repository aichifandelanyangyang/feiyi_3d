package com.feiyi.module.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 个人信息修改DTO
 *
 * @author system
 */
@Data
@Schema(description = "个人信息修改请求")
public class UserProfileDTO {

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;
}
