package com.feiyi.module.community;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.common.util.JwtUtil;
import com.feiyi.module.community.dao.CommunityFavoriteDao;
import com.feiyi.module.community.dao.CommunityLikeDao;
import com.feiyi.module.community.dao.CommunityPostDao;
import com.feiyi.module.community.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 社区服务层
 *
 * @author system
 */
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostDao postDao;
    private final CommunityFavoriteDao favoriteDao;
    private final CommunityLikeDao likeDao;
    private final JwtUtil jwtUtil;

    /**
     * 发帖（前台用户）
     */
    public ResponseDTO<Long> createPost(String authHeader, CommunityPostDTO dto) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);

        CommunityPostEntity entity = new CommunityPostEntity();
        entity.setUserId(userId);
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setImages(dto.getImages());
        entity.setStatus(0); // 待审核
        entity.setViewCount(0);
        entity.setLikeCount(0);
        postDao.insert(entity);

        return ResponseDTO.succ(entity.getId());
    }

    /**
     * 前台帖子列表（仅已通过）
     */
    public ResponseDTO<PageResultDTO<CommunityPostVO>> listApproved(CommunityQueryDTO query) {
        query.setStatus(1); // 仅显示已审核通过
        Page<CommunityPostVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<CommunityPostVO> result = postDao.listByPage(page, query);
        return ResponseDTO.succ(PageResultDTO.build(result));
    }

    /**
     * 我的帖子（前台用户）
     */
    public ResponseDTO<PageResultDTO<CommunityPostVO>> myPosts(String authHeader, CommunityQueryDTO query) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);
        query.setUserId(userId);
        Page<CommunityPostVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<CommunityPostVO> result = postDao.listByPage(page, query);
        return ResponseDTO.succ(PageResultDTO.build(result));
    }

    /**
     * 帖子详情
     */
    public ResponseDTO<CommunityPostVO> getDetail(Long id) {
        CommunityPostVO vo = postDao.getDetailById(id);
        if (vo == null) {
            return ResponseDTO.error("帖子不存在");
        }
        // 增加浏览量
        postDao.update(null, new LambdaUpdateWrapper<CommunityPostEntity>()
                .eq(CommunityPostEntity::getId, id)
                .setSql("view_count = view_count + 1"));
        vo.setViewCount(vo.getViewCount() + 1);
        return ResponseDTO.succ(vo);
    }

    /**
     * 后台管理：帖子列表（全部状态）
     */
    public ResponseDTO<PageResultDTO<CommunityPostVO>> adminList(CommunityQueryDTO query) {
        Page<CommunityPostVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<CommunityPostVO> result = postDao.listByPage(page, query);
        return ResponseDTO.succ(PageResultDTO.build(result));
    }

    /**
     * 后台管理：审核通过
     */
    public ResponseDTO<Boolean> approve(Long id) {
        CommunityPostEntity entity = postDao.selectById(id);
        if (entity == null) {
            return ResponseDTO.error("帖子不存在");
        }
        entity.setStatus(1);
        entity.setRejectReason(null);
        postDao.updateById(entity);
        return ResponseDTO.succ(true);
    }

    /**
     * 后台管理：审核拒绝
     */
    public ResponseDTO<Boolean> reject(Long id, String reason) {
        CommunityPostEntity entity = postDao.selectById(id);
        if (entity == null) {
            return ResponseDTO.error("帖子不存在");
        }
        entity.setStatus(2);
        entity.setRejectReason(reason);
        postDao.updateById(entity);
        return ResponseDTO.succ(true);
    }

    /**
     * 后台管理：删除帖子
     */
    public ResponseDTO<Boolean> delete(Long id) {
        CommunityPostEntity entity = postDao.selectById(id);
        if (entity == null) {
            return ResponseDTO.error("帖子不存在");
        }
        postDao.deleteById(id);
        return ResponseDTO.succ(true);
    }

    // ===== 帖子收藏 =====

    /**
     * 收藏帖子
     */
    public ResponseDTO<Boolean> addFavorite(String authHeader, Long postId) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);

        // 检查帖子是否存在且已审核通过
        CommunityPostEntity post = postDao.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            return ResponseDTO.error("帖子不存在或未通过审核");
        }

        // 检查是否已收藏
        Integer count = favoriteDao.checkFavorite(userId, postId);
        if (count > 0) {
            return ResponseDTO.error("已收藏过该帖子");
        }

        CommunityFavoriteEntity entity = new CommunityFavoriteEntity();
        entity.setUserId(userId);
        entity.setPostId(postId);
        favoriteDao.insert(entity);

        return ResponseDTO.succ(true);
    }

    /**
     * 取消收藏
     */
    public ResponseDTO<Boolean> removeFavorite(String authHeader, Long postId) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);

        favoriteDao.delete(new LambdaQueryWrapper<CommunityFavoriteEntity>()
                .eq(CommunityFavoriteEntity::getUserId, userId)
                .eq(CommunityFavoriteEntity::getPostId, postId));

        return ResponseDTO.succ(true);
    }

    /**
     * 检查是否已收藏
     */
    public ResponseDTO<Boolean> checkFavorite(String authHeader, Long postId) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.succ(false);
        }
        Long userId = jwtUtil.getUserId(token);

        Integer count = favoriteDao.checkFavorite(userId, postId);
        return ResponseDTO.succ(count > 0);
    }

    /**
     * 我的帖子收藏列表
     */
    public ResponseDTO<PageResultDTO<CommunityFavoriteVO>> favoriteList(String authHeader, Integer pageNum, Integer pageSize) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);

        Page<CommunityFavoriteVO> page = new Page<>(pageNum, pageSize);
        Page<CommunityFavoriteVO> result = favoriteDao.listByPage(page, userId);
        return ResponseDTO.succ(PageResultDTO.build(result));
    }

    // ===== 帖子点赞 =====

    /**
     * 点赞帖子
     */
    public ResponseDTO<Boolean> addLike(String authHeader, Long postId) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);

        // 检查帖子是否存在且已审核通过
        CommunityPostEntity post = postDao.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            return ResponseDTO.error("帖子不存在或未通过审核");
        }

        // 检查是否已点赞
        Integer count = likeDao.checkLike(userId, postId);
        if (count > 0) {
            return ResponseDTO.error("已点赞过该帖子");
        }

        CommunityLikeEntity entity = new CommunityLikeEntity();
        entity.setUserId(userId);
        entity.setPostId(postId);
        likeDao.insert(entity);

        // 增加点赞计数
        postDao.update(null, new LambdaUpdateWrapper<CommunityPostEntity>()
                .eq(CommunityPostEntity::getId, postId)
                .setSql("like_count = like_count + 1"));

        return ResponseDTO.succ(true);
    }

    /**
     * 取消点赞
     */
    public ResponseDTO<Boolean> removeLike(String authHeader, Long postId) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);

        // 检查是否已点赞
        Integer count = likeDao.checkLike(userId, postId);
        if (count == 0) {
            return ResponseDTO.error("未点赞过该帖子");
        }

        likeDao.delete(new LambdaQueryWrapper<CommunityLikeEntity>()
                .eq(CommunityLikeEntity::getUserId, userId)
                .eq(CommunityLikeEntity::getPostId, postId));

        // 减少点赞计数
        postDao.update(null, new LambdaUpdateWrapper<CommunityPostEntity>()
                .eq(CommunityPostEntity::getId, postId)
                .setSql("like_count = like_count - 1"));

        return ResponseDTO.succ(true);
    }

    /**
     * 检查是否已点赞
     */
    public ResponseDTO<Boolean> checkLike(String authHeader, Long postId) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.succ(false);
        }
        Long userId = jwtUtil.getUserId(token);

        Integer count = likeDao.checkLike(userId, postId);
        return ResponseDTO.succ(count > 0);
    }

    /**
     * 我的点赞帖子列表
     */
    public ResponseDTO<PageResultDTO<CommunityFavoriteVO>> likeList(String authHeader, Integer pageNum, Integer pageSize) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("请先登录");
        }
        Long userId = jwtUtil.getUserId(token);

        Page<CommunityFavoriteVO> page = new Page<>(pageNum, pageSize);
        Page<CommunityFavoriteVO> result = likeDao.listByPage(page, userId);
        return ResponseDTO.succ(PageResultDTO.build(result));
    }
}
