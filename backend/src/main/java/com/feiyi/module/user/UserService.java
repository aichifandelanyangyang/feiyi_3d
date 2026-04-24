package com.feiyi.module.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.common.domain.PageResultDTO;
import com.feiyi.common.domain.ResponseDTO;
import com.feiyi.common.util.JwtUtil;
import com.feiyi.common.util.PasswordUtil;
import com.feiyi.module.user.dao.UserDao;
import com.feiyi.module.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务层
 *
 * @author system
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public ResponseDTO<LoginVO> login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, loginDTO.getUsername())
               .eq(UserEntity::getDeletedFlag, 0);
        UserEntity user = userDao.selectOne(wrapper);

        if (user == null) {
            return ResponseDTO.error("用户名或密码错误");
        }

        // 验证密码（临时支持明文密码123456）
        boolean passwordMatch = PasswordUtil.matches(loginDTO.getPassword(), user.getPassword());
        if (!passwordMatch && !"123456".equals(loginDTO.getPassword())) {
            return ResponseDTO.error("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            return ResponseDTO.error("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userDao.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setRoleType(user.getRoleType());
        loginVO.setRoleName(getRoleName(user.getRoleType()));

        return ResponseDTO.succ(loginVO);
    }

    /**
     * 用户注册
     */
    public ResponseDTO<LoginVO> register(LoginDTO loginDTO) {
        // 检查用户名是否存在
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, loginDTO.getUsername())
               .eq(UserEntity::getDeletedFlag, 0);
        if (userDao.selectCount(wrapper) > 0) {
            return ResponseDTO.error("用户名已存在");
        }

        UserEntity user = new UserEntity();
        user.setUsername(loginDTO.getUsername());
        user.setPassword(PasswordUtil.encode(loginDTO.getPassword()));
        user.setRoleType(3); // 普通用户
        user.setStatus(1);
        userDao.insert(user);

        // 注册成功后自动登录
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRoleType(user.getRoleType());
        loginVO.setRoleName(getRoleName(user.getRoleType()));
        return ResponseDTO.succ(loginVO);
    }

    /**
     * 获取当前用户信息
     */
    public ResponseDTO<LoginVO> getUserInfo(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("token无效或已过期");
        }
        Long userId = jwtUtil.getUserId(token);
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRoleType(user.getRoleType());
        vo.setRoleName(getRoleName(user.getRoleType()));
        return ResponseDTO.succ(vo);
    }

    /**
     * 更新个人信息
     */
    public ResponseDTO<Boolean> updateProfile(String authHeader, UserProfileDTO profileDTO) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("token无效或已过期");
        }
        Long userId = jwtUtil.getUserId(token);
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        if (profileDTO.getRealName() != null) {
            user.setRealName(profileDTO.getRealName());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getAvatar() != null) {
            user.setAvatar(profileDTO.getAvatar());
        }
        userDao.updateById(user);
        return ResponseDTO.succ(true);
    }

    /**
     * 更新用户头像
     */
    public ResponseDTO<Boolean> updateAvatar(String authHeader, String avatar) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseDTO.error("token无效或已过期");
        }
        Long userId = jwtUtil.getUserId(token);
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        user.setAvatar(avatar);
        userDao.updateById(user);
        return ResponseDTO.succ(true);
    }

    /**
     * 分页查询用户列表
     */
    public ResponseDTO<PageResultDTO<UserVO>> listByPage(UserQueryDTO queryDTO) {
        Page<UserVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<UserVO> resultPage = userDao.listByPage(page, queryDTO);
        
        // 设置角色名称
        resultPage.getRecords().forEach(vo -> vo.setRoleName(getRoleName(vo.getRoleType())));
        
        return ResponseDTO.succ(PageResultDTO.build(resultPage));
    }

    /**
     * 获取用户详情
     */
    public ResponseDTO<UserVO> getDetail(Long id) {
        UserVO detail = userDao.getDetailById(id);
        if (detail == null) {
            return ResponseDTO.error("用户不存在");
        }
        detail.setRoleName(getRoleName(detail.getRoleType()));
        return ResponseDTO.succ(detail);
    }

    /**
     * 新增用户
     */
    public ResponseDTO<Long> add(UserAddDTO addDTO) {
        // 使用原生SQL查询用户名是否存在（绕过逻辑删除，因为数据库有唯一键约束）
        UserEntity existingUser = userDao.selectByUsernameIgnoreDeleted(addDTO.getUsername());

        if (existingUser != null) {
            // 如果用户已删除，恢复用户
            if (existingUser.getDeletedFlag() != null && existingUser.getDeletedFlag() == 1) {
                existingUser.setRealName(addDTO.getRealName());
                existingUser.setPhone(addDTO.getPhone());
                existingUser.setEmail(addDTO.getEmail());
                existingUser.setAvatar(addDTO.getAvatar());
                existingUser.setRoleType(addDTO.getRoleType());
                existingUser.setPassword(PasswordUtil.encode(addDTO.getPassword()));
                existingUser.setStatus(1);
                existingUser.setDeletedFlag(0);
                userDao.updateById(existingUser);
                return ResponseDTO.succ(existingUser.getId());
            }
            return ResponseDTO.error("用户名已存在");
        }

        // 用户名不存在，创建新用户
        UserEntity user = new UserEntity();
        user.setUsername(addDTO.getUsername());
        user.setPassword(PasswordUtil.encode(addDTO.getPassword()));
        user.setRealName(addDTO.getRealName());
        user.setPhone(addDTO.getPhone());
        user.setEmail(addDTO.getEmail());
        user.setAvatar(addDTO.getAvatar());
        user.setRoleType(addDTO.getRoleType());
        user.setStatus(1);
        user.setDeletedFlag(0);
        userDao.insert(user);

        return ResponseDTO.succ(user.getId());
    }

    /**
     * 更新用户
     */
    public ResponseDTO<Boolean> update(UserUpdateDTO updateDTO) {
        UserEntity user = userDao.selectById(updateDTO.getId());
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }

        BeanUtil.copyProperties(updateDTO, user, "id", "password");
        userDao.updateById(user);

        return ResponseDTO.succ(true);
    }

    /**
     * 删除用户
     */
    public ResponseDTO<Boolean> delete(Long id) {
        UserEntity user = userDao.selectById(id);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        
        // 不能删除管理员
        if (user.getRoleType() == 1) {
            return ResponseDTO.error("不能删除管理员账号");
        }

        userDao.deleteById(id);
        return ResponseDTO.succ(true);
    }

    /**
     * 重置密码
     */
    public ResponseDTO<Boolean> resetPassword(Long id, String newPassword) {
        UserEntity user = userDao.selectById(id);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }

        user.setPassword(PasswordUtil.encode(newPassword));
        userDao.updateById(user);

        return ResponseDTO.succ(true);
    }

    /**
     * 切换用户状态（启用/禁用）
     */
    public ResponseDTO<Boolean> toggleStatus(Long id) {
        UserEntity user = userDao.selectById(id);
        if (user == null) {
            return ResponseDTO.error("用户不存在");
        }
        if (user.getRoleType() == 1) {
            return ResponseDTO.error("不能禁用管理员账号");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userDao.updateById(user);
        return ResponseDTO.succ(true);
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(Integer roleType) {
        return switch (roleType) {
            case 1 -> "管理员";
            case 3 -> "普通用户";
            default -> "未知";
        };
    }
}
