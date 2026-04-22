package com.feiyi.module.favorite;

import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.favorite.domain.FavoriteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 *
 * @author system
 */
@Tag(name = "收藏接口")
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "收藏非遗项目")
    @PostMapping("/favorite/add/{heritageId}")
    public ResponseDTO<Boolean> addFavorite(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable Long heritageId) {
        return favoriteService.addFavorite(authHeader, heritageId);
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/favorite/remove/{heritageId}")
    public ResponseDTO<Boolean> removeFavorite(@RequestHeader("Authorization") String authHeader,
                                                @PathVariable Long heritageId) {
        return favoriteService.removeFavorite(authHeader, heritageId);
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/favorite/check/{heritageId}")
    public ResponseDTO<Boolean> isFavorite(@RequestHeader("Authorization") String authHeader,
                                            @PathVariable Long heritageId) {
        return favoriteService.isFavorite(authHeader, heritageId);
    }

    @Operation(summary = "我的收藏列表")
    @GetMapping("/favorite/list")
    public ResponseDTO<PageResultDTO<FavoriteVO>> myFavorites(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return favoriteService.myFavorites(authHeader, pageNum, pageSize);
    }
}
