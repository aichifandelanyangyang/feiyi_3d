package com.feiyi.module.exhibition.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.feiyi.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 展品实体
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_exhibit")
public class ExhibitEntity extends BaseEntity {

    /**
     * 展厅ID
     */
    private Long exhibitionId;

    /**
     * 关联非遗项目ID
     */
    private Long heritageId;

    /**
     * 展品名称
     */
    private String name;

    /**
     * 展品描述
     */
    private String description;

    /**
     * 展品图片
     */
    private String image;

    /**
     * 3D模型路径
     */
    private String modelPath;

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
     * 在展厅中的X坐标
     */
    private BigDecimal positionX;

    /**
     * 在展厅中的Y坐标
     */
    private BigDecimal positionY;

    /**
     * 在展厅中的Z坐标
     */
    private BigDecimal positionZ;

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
