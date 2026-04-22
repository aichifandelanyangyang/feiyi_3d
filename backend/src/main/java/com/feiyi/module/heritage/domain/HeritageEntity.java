package com.feiyi.module.heritage.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.feiyi.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 非遗项目实体
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_heritage")
public class HeritageEntity extends BaseEntity {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 级别：国家级、省级、市级、县级
     */
    private String level;

    /**
     * 所属地区
     */
    private String region;

    /**
     * 申报年份
     */
    private String declareYear;

    /**
     * 传承人
     */
    private String inheritor;

    /**
     * 简介
     */
    private String description;

    /**
     * 详细内容
     */
    private String content;

    /**
     * 历史渊源
     */
    private String history;

    /**
     * 工艺特点
     */
    private String feature;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 删除标记 0未删除 1已删除
     */
    private Integer deletedFlag;
}
