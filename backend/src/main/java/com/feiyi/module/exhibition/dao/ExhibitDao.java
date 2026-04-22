package com.feiyi.module.exhibition.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.module.exhibition.domain.ExhibitEntity;
import com.feiyi.module.exhibition.domain.ExhibitQueryDTO;
import com.feiyi.module.exhibition.domain.ExhibitVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 展品数据访问层
 *
 * @author system
 */
@Mapper
public interface ExhibitDao extends BaseMapper<ExhibitEntity> {

    /**
     * 分页查询展品列表
     */
    Page<ExhibitVO> listByPage(Page<?> page, @Param("query") ExhibitQueryDTO query);

    /**
     * 根据展厅ID获取展品列表
     *
     * @param exhibitionId 展厅ID
     * @return 展品列表
     */
    List<ExhibitVO> listByExhibitionId(@Param("exhibitionId") Long exhibitionId);

    /**
     * 根据ID获取展品详情
     *
     * @param id 展品ID
     * @return 展品详情
     */
    ExhibitVO getDetailById(@Param("id") Long id);

    /**
     * 根据名称获取展品详情（精确匹配）
     *
     * @param name 展品名称
     * @return 展品详情
     */
    ExhibitVO getDetailByName(@Param("name") String name);

    /**
     * 根据名称获取展品详情（模糊匹配，按名称长度排序取最短的）
     *
     * @param name 展品名称
     * @return 展品详情
     */
    ExhibitVO getDetailByNameLike(@Param("name") String name);
}
