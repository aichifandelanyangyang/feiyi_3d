package com.feiyi.module.exhibition.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feiyi.module.exhibition.domain.ExhibitionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 展厅数据访问层
 *
 * @author system
 */
@Mapper
public interface ExhibitionDao extends BaseMapper<ExhibitionEntity> {

    /**
     * 增加访问量
     *
     * @param id 展厅ID
     * @return 影响行数
     */
    int incrementVisitCount(@Param("id") Long id);
}
