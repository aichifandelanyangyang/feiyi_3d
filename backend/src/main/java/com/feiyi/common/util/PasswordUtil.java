package com.feiyi.common.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类
 *
 * @author system
 */
public class PasswordUtil {

    /**
     * 加密密码
     */
    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * 验证密码
     */
    public static boolean matches(String password, String encodedPassword) {
        return BCrypt.checkpw(password, encodedPassword);
    }
}
