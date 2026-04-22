package com.feiyi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 非遗3D数字化交互平台启动类
 *
 * @author system
 */
@SpringBootApplication
@MapperScan("com.feiyi.module.*.dao")
public class FeiyiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeiyiApplication.class, args);
        System.out.println("============================================");
        System.out.println("    非遗3D数字化交互平台启动成功！");
        System.out.println("    接口文档: http://localhost:8080/api/doc.html");
        System.out.println("============================================");
    }
}
