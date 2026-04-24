package com.feiyi.module.ai.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.feiyi.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI知识库实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_knowledge")
public class KnowledgeEntity extends BaseEntity {

    private String title;

    private String content;

    private String category;

    /**
     * 删除标记 0未删除 1已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deletedFlag;
}
