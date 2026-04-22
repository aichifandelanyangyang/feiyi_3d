package com.feiyi.module.exhibition;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.exhibition.dao.ExhibitDao;
import com.feiyi.module.exhibition.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 展品服务层
 *
 * @author system
 */
@Service
@RequiredArgsConstructor
public class ExhibitService {

    private final ExhibitDao exhibitDao;

    /**
     * 分页查询展品列表
     */
    public ResponseDTO<PageResultDTO<ExhibitVO>> listByPage(ExhibitQueryDTO queryDTO) {
        Page<ExhibitVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ExhibitVO> resultPage = exhibitDao.listByPage(page, queryDTO);
        return ResponseDTO.succ(PageResultDTO.build(resultPage));
    }

    /**
     * 获取展品详情
     */
    public ResponseDTO<ExhibitVO> getDetail(Long id) {
        ExhibitVO detail = exhibitDao.getDetailById(id);
        if (detail == null) {
            return ResponseDTO.error("展品不存在");
        }
        return ResponseDTO.succ(detail);
    }

    /**
     * 根据名称获取展品详情（精确匹配优先，找不到再模糊匹配）
     */
    public ResponseDTO<ExhibitVO> getDetailByName(String name) {
        // 先精确匹配
        ExhibitVO detail = exhibitDao.getDetailByName(name);
        if (detail == null) {
            // 精确匹配找不到，再模糊匹配（按名称长度排序取最短的）
            detail = exhibitDao.getDetailByNameLike(name);
        }
        if (detail == null) {
            return ResponseDTO.error("展品不存在");
        }
        return ResponseDTO.succ(detail);
    }

    /**
     * 根据展厅ID获取展品列表
     */
    public ResponseDTO<List<ExhibitVO>> listByExhibitionId(Long exhibitionId) {
        List<ExhibitVO> list = exhibitDao.listByExhibitionId(exhibitionId);
        return ResponseDTO.succ(list);
    }

    /**
     * 新增展品
     */
    public ResponseDTO<Long> add(ExhibitAddDTO addDTO) {
        ExhibitEntity exhibit = new ExhibitEntity();
        BeanUtil.copyProperties(addDTO, exhibit);
        exhibitDao.insert(exhibit);
        return ResponseDTO.succ(exhibit.getId());
    }

    /**
     * 更新展品
     */
    public ResponseDTO<Boolean> update(ExhibitAddDTO updateDTO, Long id) {
        ExhibitEntity exhibit = exhibitDao.selectById(id);
        if (exhibit == null) {
            return ResponseDTO.error("展品不存在");
        }
        BeanUtil.copyProperties(updateDTO, exhibit, "id");
        exhibitDao.updateById(exhibit);
        return ResponseDTO.succ(true);
    }

    /**
     * 删除展品
     */
    public ResponseDTO<Boolean> delete(Long id) {
        ExhibitEntity exhibit = exhibitDao.selectById(id);
        if (exhibit == null) {
            return ResponseDTO.error("展品不存在");
        }
        exhibitDao.deleteById(id);
        return ResponseDTO.succ(true);
    }
}
