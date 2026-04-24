package com.feiyi.module.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feiyi.module.user.domain.UserEntity;
import com.feiyi.module.user.domain.UserQueryDTO;
import com.feiyi.module.user.domain.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 *
 * @author system
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    /**
     * 分页查询用户列表
     */
    Page<UserVO> listByPage(Page<?> page, @Param("query") UserQueryDTO query);

    /**
     * 根据ID获取用户详情
     */
    UserVO getDetailById(@Param("id") Long id);

    /**
     * 根据用户名查询用户（绕过逻辑删除）
     */
    @Select("SELECT * FROM t_user WHERE username = #{username}")
    UserEntity selectByUsernameIgnoreDeleted(@Param("username") String username);
}
