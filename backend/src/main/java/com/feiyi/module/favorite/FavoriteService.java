package com.feiyi.module.favorite;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.common.util.JwtUtil;
import com.feiyi.module.favorite.dao.FavoriteDao;
import com.feiyi.module.favorite.domain.FavoriteEntity;
import com.feiyi.module.favorite.domain.FavoriteVO;
import com.feiyi.module.heritage.dao.HeritageDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 收藏服务层
 *
 * @author system
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteDao favoriteDao;
    private final HeritageDao heritageDao;
    private final JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserId(token);
    }

    /**
     * 收藏非遗项目
     */
    public ResponseDTO<Boolean> addFavorite(String authHeader, Long heritageId) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseDTO.error("请先登录");

        // 检查是否已收藏
        LambdaQueryWrapper<FavoriteEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteEntity::getUserId, userId)
               .eq(FavoriteEntity::getHeritageId, heritageId);
        if (favoriteDao.selectCount(wrapper) > 0) {
            return ResponseDTO.error("已收藏该项目");
        }

        FavoriteEntity entity = new FavoriteEntity();
        entity.setUserId(userId);
        entity.setHeritageId(heritageId);
        favoriteDao.insert(entity);

        // 更新收藏数
        heritageDao.incrementFavoriteCount(heritageId);
        return ResponseDTO.succ(true);
    }

    /**
     * 取消收藏
     */
    public ResponseDTO<Boolean> removeFavorite(String authHeader, Long heritageId) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseDTO.error("请先登录");

        LambdaQueryWrapper<FavoriteEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteEntity::getUserId, userId)
               .eq(FavoriteEntity::getHeritageId, heritageId);
        int deleted = favoriteDao.delete(wrapper);

        // 更新收藏数（仅在成功删除时）
        if (deleted > 0) {
            heritageDao.decrementFavoriteCount(heritageId);
        }
        return ResponseDTO.succ(true);
    }

    /**
     * 检查是否已收藏
     */
    public ResponseDTO<Boolean> isFavorite(String authHeader, Long heritageId) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseDTO.succ(false);

        LambdaQueryWrapper<FavoriteEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteEntity::getUserId, userId)
               .eq(FavoriteEntity::getHeritageId, heritageId);
        return ResponseDTO.succ(favoriteDao.selectCount(wrapper) > 0);
    }

    /**
     * 我的收藏列表
     */
    public ResponseDTO<PageResultDTO<FavoriteVO>> myFavorites(String authHeader, Integer pageNum, Integer pageSize) {
        Long userId = getUserId(authHeader);
        if (userId == null) return ResponseDTO.error("请先登录");

        Page<FavoriteVO> page = new Page<>(pageNum, pageSize);
        Page<FavoriteVO> result = favoriteDao.listByUserId(page, userId);
        return ResponseDTO.succ(PageResultDTO.build(result));
    }
}
