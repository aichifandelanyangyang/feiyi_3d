package com.feiyi.module.ai.domain;

import lombok.Data;

/**
 * 展品链接 - 用于AI对话响应中返回相关展品信息
 */
@Data
public class ExhibitLink {

    /**
     * 展品ID
     */
    private Long id;

    /**
     * 展品名称
     */
    private String name;

    /**
     * 展品图片
     */
    private String image;

    /**
     * 所属展厅ID
     */
    private Long exhibitionId;
}