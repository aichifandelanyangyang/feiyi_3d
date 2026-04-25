package com.feiyi.module.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.module.community.domain.CommunityFavoriteEntity;
import com.feiyi.module.community.domain.CommunityFavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 社区帖子收藏DAO
 *
 * @author system
 */
@Mapper
public interface CommunityFavoriteDao extends BaseMapper<CommunityFavoriteEntity> {

    /**
     * 分页查询用户收藏的帖子列表
     */
    Page<CommunityFavoriteVO> listByPage(Page<?> page, @Param("userId") Long userId);

    /**
     * 检查用户是否已收藏帖子
     */
    Integer checkFavorite(@Param("userId") Long userId, @Param("postId") Long postId);
}