package com.feiyi.module.exhibition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.exhibition.dao.ExhibitDao;
import com.feiyi.module.exhibition.dao.ExhibitionDao;
import com.feiyi.module.exhibition.domain.ExhibitionEntity;
import com.feiyi.module.exhibition.domain.ExhibitionVO;
import com.feiyi.module.exhibition.domain.ExhibitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 展厅服务层
 *
 * @author system
 */
@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionDao exhibitionDao;
    private final ExhibitDao exhibitDao;

    /**
     * 获取展厅信息
     *
     * @return 展厅信息
     */
    public ResponseDTO<ExhibitionVO> getExhibitionInfo() {
        LambdaQueryWrapper<ExhibitionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExhibitionEntity::getStatus, 1)
               .eq(ExhibitionEntity::getDeletedFlag, 0)
               .last("LIMIT 1");
        ExhibitionEntity entity = exhibitionDao.selectOne(wrapper);
        
        if (entity == null) {
            return ResponseDTO.error("展厅不存在");
        }

        ExhibitionVO vo = new ExhibitionVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 增加访问量
        exhibitionDao.incrementVisitCount(entity.getId());
        
        return ResponseDTO.succ(vo);
    }

    /**
     * 获取展品列表
     *
     * @param exhibitionId 展厅ID
     * @return 展品列表
     */
    public ResponseDTO<List<ExhibitVO>> listExhibit(Long exhibitionId) {
        List<ExhibitVO> list = exhibitDao.listByExhibitionId(exhibitionId);
        return ResponseDTO.succ(list);
    }

    /**
     * 获取展品详情
     *
     * @param id 展品ID
     * @return 展品详情
     */
    public ResponseDTO<ExhibitVO> getExhibitDetail(Long id) {
        ExhibitVO detail = exhibitDao.getDetailById(id);
        if (detail == null) {
            return ResponseDTO.error("展品不存在");
        }
        return ResponseDTO.succ(detail);
    }
}
