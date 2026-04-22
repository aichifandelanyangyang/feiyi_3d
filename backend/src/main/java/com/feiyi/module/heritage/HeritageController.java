package com.feiyi.module.heritage;

import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.heritage.domain.HeritageCategoryEntity;
import com.feiyi.module.heritage.domain.HeritageEntity;
import com.feiyi.module.heritage.domain.HeritageQueryDTO;
import com.feiyi.module.heritage.domain.HeritageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 非遗项目控制器
 *
 * @author system
 */
@Tag(name = "非遗项目接口")
@RestController
@RequiredArgsConstructor
public class HeritageController {

    private final HeritageService heritageService;

    // ========== 非遗项目（公开接口） ==========

    @Operation(summary = "分页查询非遗项目列表 @author system")
    @GetMapping("/heritage/list")
    public ResponseDTO<PageResultDTO<HeritageVO>> list(HeritageQueryDTO queryDTO) {
        return heritageService.listByPage(queryDTO);
    }

    @Operation(summary = "获取非遗项目详情 @author system")
    @GetMapping("/heritage/get/{id}")
    public ResponseDTO<HeritageVO> getDetail(@PathVariable Long id) {
        return heritageService.getDetail(id);
    }

    @Operation(summary = "获取非遗分类列表 @author system")
    @GetMapping("/heritage/category/list")
    public ResponseDTO<List<HeritageCategoryEntity>> listCategory() {
        return heritageService.listCategory();
    }

    @Operation(summary = "搜索非遗项目 @author system")
    @GetMapping("/heritage/search")
    public ResponseDTO<List<HeritageVO>> search(@RequestParam String keyword) {
        return heritageService.search(keyword);
    }

    // ========== 非遗分类（管理接口） ==========

    @Operation(summary = "新增非遗分类")
    @PostMapping("/admin/heritage/category/add")
    public ResponseDTO<Long> addCategory(@RequestBody HeritageCategoryEntity entity) {
        return heritageService.addCategory(entity);
    }

    @Operation(summary = "更新非遗分类")
    @PostMapping("/admin/heritage/category/update/{id}")
    public ResponseDTO<Boolean> updateCategory(@PathVariable Long id, @RequestBody HeritageCategoryEntity entity) {
        return heritageService.updateCategory(id, entity);
    }

    @Operation(summary = "删除非遗分类")
    @PostMapping("/admin/heritage/category/delete/{id}")
    public ResponseDTO<Boolean> deleteCategory(@PathVariable Long id) {
        return heritageService.deleteCategory(id);
    }

    // ========== 非遗项目（管理接口） ==========

    @Operation(summary = "新增非遗项目")
    @PostMapping("/admin/heritage/add")
    public ResponseDTO<Long> addHeritage(@RequestBody HeritageEntity entity) {
        return heritageService.addHeritage(entity);
    }

    @Operation(summary = "更新非遗项目")
    @PostMapping("/admin/heritage/update/{id}")
    public ResponseDTO<Boolean> updateHeritage(@PathVariable Long id, @RequestBody HeritageEntity entity) {
        return heritageService.updateHeritage(id, entity);
    }

    @Operation(summary = "删除非遗项目")
    @PostMapping("/admin/heritage/delete/{id}")
    public ResponseDTO<Boolean> deleteHeritage(@PathVariable Long id) {
        return heritageService.deleteHeritage(id);
    }
}
