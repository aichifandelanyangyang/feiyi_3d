package com.feiyi.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @author system
 */
@Data
public class ResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public ResponseDTO() {
    }

    public ResponseDTO(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> ResponseDTO<T> succ() {
        return new ResponseDTO<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseDTO<T> succ(T data) {
        return new ResponseDTO<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> ResponseDTO<T> succ(String msg, T data) {
        return new ResponseDTO<>(200, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> ResponseDTO<T> error(String msg) {
        return new ResponseDTO<>(500, msg, null);
    }

    /**
     * 失败响应（带错误码）
     */
    public static <T> ResponseDTO<T> error(Integer code, String msg) {
        return new ResponseDTO<>(code, msg, null);
    }

    /**
     * 根据条件返回结果
     */
    public static <T> ResponseDTO<T> wrap(ResponseCodeConst codeConst) {
        return new ResponseDTO<>(codeConst.getCode(), codeConst.getMsg(), null);
    }
}
