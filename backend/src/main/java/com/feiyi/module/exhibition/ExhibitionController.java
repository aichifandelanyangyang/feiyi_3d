package com.feiyi.module.exhibition;

import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.exhibition.domain.ExhibitionVO;
import com.feiyi.module.exhibition.domain.ExhibitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 展厅控制器
 *
 * @author system
 */
@Tag(name = "展厅接口")
@RestController
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @Operation(summary = "获取展厅信息 @author system")
    @GetMapping("/exhibition/info")
    public ResponseDTO<ExhibitionVO> getInfo() {
        return exhibitionService.getExhibitionInfo();
    }

    @Operation(summary = "获取展品列表 @author system")
    @GetMapping("/exhibition/exhibit/list")
    public ResponseDTO<List<ExhibitVO>> listExhibit(@RequestParam Long exhibitionId) {
        return exhibitionService.listExhibit(exhibitionId);
    }

    @Operation(summary = "获取展品详情 @author system")
    @GetMapping("/exhibition/exhibit/get/{id}")
    public ResponseDTO<ExhibitVO> getExhibitDetail(@PathVariable Long id) {
        return exhibitionService.getExhibitDetail(id);
    }
}
