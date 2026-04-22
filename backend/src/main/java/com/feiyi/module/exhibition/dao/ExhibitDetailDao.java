package com.feiyi.module.exhibition.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feiyi.module.exhibition.domain.ExhibitDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 展品详情数据访问层
 *
 * @author system
 */
@Mapper
public interface ExhibitDetailDao extends BaseMapper<ExhibitDetailEntity> {

    /**
     * 根据展品ID获取详情
     */
    ExhibitDetailEntity getByExhibitId(@Param("exhibitId") Long exhibitId);
}
