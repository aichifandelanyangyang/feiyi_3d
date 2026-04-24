package com.feiyi.module.heritage.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
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
     * 所属地区（申报地区）
     */
    private String region;

    /**
     * 保护单位
     */
    private String protectionUnit;

    /**
     * 公布时间
     */
    private String publishTime;

    /**
     * 传承人
     */
    private String inheritor;

    /**
     * 简介
     */
    private String description;

    /**
     * 历史渊源
     */
    private String history;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 浏览次数（浏览量）
     */
    private Integer viewCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 删除标记 0未删除 1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deletedFlag;
}
