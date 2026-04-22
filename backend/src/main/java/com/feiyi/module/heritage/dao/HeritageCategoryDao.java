package com.feiyi.module.heritage.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feiyi.module.heritage.domain.HeritageCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 非遗分类数据访问层
 *
 * @author system
 */
@Mapper
public interface HeritageCategoryDao extends BaseMapper<HeritageCategoryEntity> {

}
