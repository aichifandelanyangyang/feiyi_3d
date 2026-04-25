package com.feiyi.module.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.module.community.domain.CommunityFavoriteVO;
import com.feiyi.module.community.domain.CommunityLikeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 社区帖子点赞DAO
 *
 * @author system
 */
@Mapper
public interface CommunityLikeDao extends BaseMapper<CommunityLikeEntity> {

    /**
     * 检查用户是否已点赞帖子
     */
    Integer checkLike(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 分页查询用户点赞的帖子列表
     */
    Page<CommunityFavoriteVO> listByPage(Page<?> page, @Param("userId") Long userId);
}