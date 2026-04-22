package com.feiyi.module.exhibition;

import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.exhibition.domain.ExhibitAddDTO;
import com.feiyi.module.exhibition.domain.ExhibitQueryDTO;
import com.feiyi.module.exhibition.domain.ExhibitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 展品控制器
 *
 * @author system
 */
@Tag(name = "展品接口")
@RestController
@RequiredArgsConstructor
public class ExhibitController {

    private final ExhibitService exhibitService;

    @Operation(summary = "分页查询展品列表")
    @GetMapping("/exhibit/list")
    public ResponseDTO<PageResultDTO<ExhibitVO>> list(ExhibitQueryDTO queryDTO) {
        return exhibitService.listByPage(queryDTO);
    }

    @Operation(summary = "获取展品详情")
    @GetMapping("/exhibit/get/{id}")
    public ResponseDTO<ExhibitVO> getDetail(@PathVariable Long id) {
        return exhibitService.getDetail(id);
    }

    @Operation(summary = "根据名称获取展品详情")
    @GetMapping("/exhibit/getByName")
    public ResponseDTO<ExhibitVO> getDetailByName(@RequestParam String name) {
        return exhibitService.getDetailByName(name);
    }

    @Operation(summary = "根据展厅ID获取展品列表")
    @GetMapping("/exhibit/listByExhibition/{exhibitionId}")
    public ResponseDTO<List<ExhibitVO>> listByExhibitionId(@PathVariable Long exhibitionId) {
        return exhibitService.listByExhibitionId(exhibitionId);
    }

    @Operation(summary = "新增展品")
    @PostMapping("/admin/exhibit/add")
    public ResponseDTO<Long> add(@Valid @RequestBody ExhibitAddDTO addDTO) {
        return exhibitService.add(addDTO);
    }

    @Operation(summary = "更新展品")
    @PostMapping("/admin/exhibit/update/{id}")
    public ResponseDTO<Boolean> update(@PathVariable Long id, @Valid @RequestBody ExhibitAddDTO updateDTO) {
        return exhibitService.update(updateDTO, id);
    }

    @Operation(summary = "删除展品")
    @PostMapping("/admin/exhibit/delete/{id}")
    public ResponseDTO<Boolean> delete(@PathVariable Long id) {
        return exhibitService.delete(id);
    }
}
