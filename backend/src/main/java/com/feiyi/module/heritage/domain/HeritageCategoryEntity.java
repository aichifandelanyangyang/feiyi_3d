package com.feiyi.module.heritage.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.feiyi.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 非遗分类实体
 *
 * @author system
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_heritage_category")
public class HeritageCategoryEntity extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 删除标记 0未删除 1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deletedFlag;
}
