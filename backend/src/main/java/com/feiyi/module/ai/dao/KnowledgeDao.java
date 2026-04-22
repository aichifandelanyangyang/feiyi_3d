package com.feiyi.module.ai.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feiyi.module.ai.domain.KnowledgeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI知识库数据访问层
 */
@Mapper
public interface KnowledgeDao extends BaseMapper<KnowledgeEntity> {
}
