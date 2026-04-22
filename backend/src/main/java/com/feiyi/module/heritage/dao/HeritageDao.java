package com.feiyi.module.heritage.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.module.heritage.domain.HeritageEntity;
import com.feiyi.module.heritage.domain.HeritageQueryDTO;
import com.feiyi.module.heritage.domain.HeritageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 非遗项目数据访问层
 *
 * @author system
 */
@Mapper
public interface HeritageDao extends BaseMapper<HeritageEntity> {

    /**
     * 分页查询非遗项目列表
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<HeritageVO> listByPage(Page<HeritageVO> page, @Param("query") HeritageQueryDTO queryDTO);

    /**
     * 根据ID获取非遗项目详情
     *
     * @param id 项目ID
     * @return 项目详情
     */
    HeritageVO getDetailById(@Param("id") Long id);

    /**
     * 增加浏览次数
     *
     * @param id 项目ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);
}
