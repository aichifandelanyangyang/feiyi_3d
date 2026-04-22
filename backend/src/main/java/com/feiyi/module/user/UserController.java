package com.feiyi.module.user;

import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.module.user.domain.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author system
 */
@Tag(name = "用户接口")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseDTO<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseDTO<LoginVO> register(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.register(loginDTO);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user/info")
    public ResponseDTO<LoginVO> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        return userService.getUserInfo(authHeader);
    }

    @Operation(summary = "更新用户头像")
    @PostMapping("/user/avatar")
    public ResponseDTO<Boolean> updateAvatar(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam String avatar) {
        return userService.updateAvatar(authHeader, avatar);
    }

    @Operation(summary = "更新个人信息")
    @PostMapping("/user/profile")
    public ResponseDTO<Boolean> updateProfile(@RequestHeader("Authorization") String authHeader,
                                               @RequestBody UserProfileDTO profileDTO) {
        return userService.updateProfile(authHeader, profileDTO);
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/admin/user/list")
    public ResponseDTO<PageResultDTO<UserVO>> list(UserQueryDTO queryDTO) {
        return userService.listByPage(queryDTO);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/admin/user/get/{id}")
    public ResponseDTO<UserVO> getDetail(@PathVariable Long id) {
        return userService.getDetail(id);
    }

    @Operation(summary = "新增用户")
    @PostMapping("/admin/user/add")
    public ResponseDTO<Long> add(@Valid @RequestBody UserAddDTO addDTO) {
        return userService.add(addDTO);
    }

    @Operation(summary = "更新用户")
    @PostMapping("/admin/user/update")
    public ResponseDTO<Boolean> update(@Valid @RequestBody UserUpdateDTO updateDTO) {
        return userService.update(updateDTO);
    }

    @Operation(summary = "删除用户")
    @PostMapping("/admin/user/delete/{id}")
    public ResponseDTO<Boolean> delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    @Operation(summary = "重置密码")
    @PostMapping("/admin/user/resetPassword")
    public ResponseDTO<Boolean> resetPassword(@RequestParam Long id, @RequestParam String newPassword) {
        return userService.resetPassword(id, newPassword);
    }

    @Operation(summary = "切换用户状态（启用/禁用）")
    @PostMapping("/admin/user/toggleStatus/{id}")
    public ResponseDTO<Boolean> toggleStatus(@PathVariable Long id) {
        return userService.toggleStatus(id);
    }
}
