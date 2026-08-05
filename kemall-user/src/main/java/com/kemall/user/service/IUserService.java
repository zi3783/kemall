package com.kemall.user.service;

import com.kemall.user.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kemall.user.domain.dto.UserDTO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author author
 * @since 2026-08-05
 */
public interface IUserService extends IService<User> {

    void registerUser(UserDTO user);

    String login(String username, String password);
}
