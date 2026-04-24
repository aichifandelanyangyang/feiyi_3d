package com.feiyi.module.exhibition.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.feiyi.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 展厅实体
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_exhibition")
public class ExhibitionEntity extends BaseEntity {

    /**
     * 展厅名称
     */
    private String name;

    /**
     * 展厅描述
     */
    private String description;

    /**
     * 展厅封面图片
     */
    private String coverImage;

    /**
     * 3D模型文件路径
     */
    private String modelPath;

    /**
     * 访问量
     */
    private Integer visitCount;

    /**
     * 状态 0禁用 1启用
     */
    private Integer status;

    /**
     * 删除标记 0未删除 1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deletedFlag;
}
