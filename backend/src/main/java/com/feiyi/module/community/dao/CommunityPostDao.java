package com.feiyi.module.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.module.community.domain.CommunityPostEntity;
import com.feiyi.module.community.domain.CommunityPostVO;
import com.feiyi.module.community.domain.CommunityQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 社区帖子DAO
 *
 * @author system
 */
@Mapper
public interface CommunityPostDao extends BaseMapper<CommunityPostEntity> {

    /**
     * 分页查询帖子列表
     */
    Page<CommunityPostVO> listByPage(Page<?> page, @Param("query") CommunityQueryDTO query);

    /**
     * 根据ID查询帖子详情（含用户信息）
     */
    CommunityPostVO getDetailById(@Param("id") Long id);
}
