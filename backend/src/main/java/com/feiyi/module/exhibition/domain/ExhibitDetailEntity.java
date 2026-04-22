package com.feiyi.module.exhibition.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.feiyi.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 展品详情实体
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_exhibit_detail")
public class ExhibitDetailEntity extends BaseEntity {

    /**
     * 展品ID
     */
    private Long exhibitId;

    /**
     * 展品类别
     */
    private String category;

    /**
     * 年代
     */
    private String era;

    /**
     * 产地
     */
    private String origin;

    /**
     * 材质
     */
    private String material;

    /**
     * 尺寸
     */
    private String size;

    /**
     * 历史背景
     */
    private String history;

    /**
     * 工艺特点
     */
    private String craft;

    /**
     * 文化价值
     */
    private String culturalValue;

    /**
     * 语音讲解路径
     */
    private String audioPath;

    /**
     * 视频路径
     */
    private String videoPath;
}
