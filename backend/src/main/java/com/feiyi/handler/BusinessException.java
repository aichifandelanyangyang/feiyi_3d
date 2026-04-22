package com.feiyi.handler;

import com.feiyi.common.domain.ResponseCodeConst;
import lombok.Getter;

/**
 * 业务异常类
 *
 * @author system
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCodeConst codeConst) {
        super(codeConst.getMsg());
        this.code = codeConst.getCode();
    }
}
