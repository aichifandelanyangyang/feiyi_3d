package com.feiyi.module.favorite.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.module.favorite.domain.FavoriteEntity;
import com.feiyi.module.favorite.domain.FavoriteVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏DAO
 *
 * @author system
 */
@Mapper
public interface FavoriteDao extends BaseMapper<FavoriteEntity> {

    /**
     * 分页查询用户收藏列表（含非遗项目信息）
     */
    Page<FavoriteVO> listByUserId(Page<?> page, @Param("userId") Long userId);
}
