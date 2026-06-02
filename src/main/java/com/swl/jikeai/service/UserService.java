package com.swl.jikeai.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.swl.jikeai.model.dto.user.UserQueryRequest;
import com.swl.jikeai.model.entity.User;
import com.swl.jikeai.model.vo.LoginUserVO;
import com.swl.jikeai.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author mubaodian
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @param request      请求
     * @return 脱敏后的用户
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 用户退出登录
     *
     * @param request 请求
     * @return 是否成功
     */
    Boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏后的登录用户信息
     *
     * @param user 登录用户
     * @return 脱敏后的用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList 用户列表
     * @return 脱敏后的用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据查询请求获取queryWrapper
     * @param userQueryRequest 查询请求
     * @return queryWrapper
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
    /**
     * 密码加密
     *
     * @param originalPassword 原始密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String originalPassword);
}
