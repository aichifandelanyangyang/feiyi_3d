package com.feiyi.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码常量枚举
 *
 * @author system
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeConst {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 参数错误
     */
    ERROR_PARAM(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统错误"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(1001, "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_EXIST(1002, "数据已存在");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String msg;
}
