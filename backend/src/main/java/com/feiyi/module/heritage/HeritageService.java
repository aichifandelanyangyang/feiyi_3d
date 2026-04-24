package com.feiyi.module.heritage;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.heritage.dao.HeritageCategoryDao;
import com.feiyi.module.heritage.dao.HeritageDao;
import com.feiyi.module.heritage.domain.HeritageCategoryEntity;
import com.feiyi.module.heritage.domain.HeritageEntity;
import com.feiyi.module.heritage.domain.HeritageQueryDTO;
import com.feiyi.module.heritage.domain.HeritageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 非遗项目服务层
 *
 * @author system
 */
@Service
@RequiredArgsConstructor
public class HeritageService {

    private final HeritageDao heritageDao;
    private final HeritageCategoryDao heritageCategoryDao;

    /**
     * 分页查询非遗项目列表
     */
    public ResponseDTO<PageResultDTO<HeritageVO>> listByPage(HeritageQueryDTO queryDTO) {
        Page<HeritageVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<HeritageVO> resultPage = heritageDao.listByPage(page, queryDTO);
        return ResponseDTO.succ(PageResultDTO.build(resultPage));
    }

    /**
     * 获取非遗项目详情
     */
    public ResponseDTO<HeritageVO> getDetail(Long id) {
        HeritageVO detail = heritageDao.getDetailById(id);
        if (detail == null) {
            return ResponseDTO.error("项目不存在");
        }
        heritageDao.incrementViewCount(id);
        return ResponseDTO.succ(detail);
    }

    /**
     * 获取分类列表
     */
    public ResponseDTO<List<HeritageCategoryEntity>> listCategory() {
        LambdaQueryWrapper<HeritageCategoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeritageCategoryEntity::getDeletedFlag, 0)
               .orderByDesc(HeritageCategoryEntity::getCreateTime);
        List<HeritageCategoryEntity> list = heritageCategoryDao.selectList(wrapper);
        return ResponseDTO.succ(list);
    }

    /**
     * 搜索非遗项目
     */
    public ResponseDTO<List<HeritageVO>> search(String keyword) {
        HeritageQueryDTO queryDTO = new HeritageQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(20);
        Page<HeritageVO> page = new Page<>(1, 20);
        Page<HeritageVO> resultPage = heritageDao.listByPage(page, queryDTO);
        return ResponseDTO.succ(resultPage.getRecords());
    }

    // ========== 非遗分类管理 ==========

    /**
     * 新增分类
     */
    public ResponseDTO<Long> addCategory(HeritageCategoryEntity entity) {
        entity.setDeletedFlag(0);
        heritageCategoryDao.insert(entity);
        return ResponseDTO.succ(entity.getId());
    }

    /**
     * 更新分类
     */
    public ResponseDTO<Boolean> updateCategory(Long id, HeritageCategoryEntity entity) {
        HeritageCategoryEntity existing = heritageCategoryDao.selectById(id);
        if (existing == null) {
            return ResponseDTO.error("分类不存在");
        }
        BeanUtil.copyProperties(entity, existing, "id", "createTime", "deletedFlag");
        heritageCategoryDao.updateById(existing);
        return ResponseDTO.succ(true);
    }

    /**
     * 删除分类（逻辑删除）
     */
    public ResponseDTO<Boolean> deleteCategory(Long id) {
        HeritageCategoryEntity existing = heritageCategoryDao.selectById(id);
        if (existing == null) {
            return ResponseDTO.error("分类不存在或已删除");
        }
        // 使用 MyBatis-Plus 逻辑删除
        heritageCategoryDao.deleteById(id);
        return ResponseDTO.succ(true);
    }

    // ========== 非遗项目管理 ==========

    /**
     * 新增非遗项目
     */
    public ResponseDTO<Long> addHeritage(HeritageEntity entity) {
        entity.setDeletedFlag(0);
        if (entity.getViewCount() == null) {
            entity.setViewCount(0);
        }
        if (entity.getFavoriteCount() == null) {
            entity.setFavoriteCount(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        heritageDao.insert(entity);
        return ResponseDTO.succ(entity.getId());
    }

    /**
     * 更新非遗项目
     */
    public ResponseDTO<Boolean> updateHeritage(Long id, HeritageEntity entity) {
        HeritageEntity existing = heritageDao.selectById(id);
        if (existing == null) {
            return ResponseDTO.error("项目不存在");
        }
        BeanUtil.copyProperties(entity, existing, "id", "createTime", "deletedFlag", "viewCount");
        heritageDao.updateById(existing);
        return ResponseDTO.succ(true);
    }

    /**
     * 删除非遗项目（逻辑删除）
     */
    public ResponseDTO<Boolean> deleteHeritage(Long id) {
        HeritageEntity existing = heritageDao.selectById(id);
        if (existing == null) {
            return ResponseDTO.error("项目不存在或已删除");
        }
        // 使用 MyBatis-Plus 逻辑删除，会自动将 deleted_flag 设置为 1
        heritageDao.deleteById(id);
        return ResponseDTO.succ(true);
    }
}
