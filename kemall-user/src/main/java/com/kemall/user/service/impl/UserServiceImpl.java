package com.kemall.user.service.impl;

import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.JwtUtil;
import com.kemall.common.utils.SnowflakeGenerator;
import com.kemall.user.domain.po.User;
import com.kemall.user.domain.dto.UserDTO;
import com.kemall.user.enums.AccountStatusEnum;
import com.kemall.user.enums.UserTypeEnum;
import com.kemall.user.mapper.UserMapper;
import com.kemall.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-05
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final SnowflakeGenerator snowflakeGenerator;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Override
    public void registerUser(UserDTO user) {
        if(user.getUsername()==null || user.getPassword()==null){
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        boolean exists = lambdaQuery().eq(User::getUsername, user.getUsername()).exists();
        if(exists){
            throw new BusinessException("用户已经存在");
        }

        long id = snowflakeGenerator.nextId();
        String nickname = "用户" + id;

        String password = passwordEncoder.encode(user.getPassword());

        User u = new User();
        u.setUsername(user.getUsername());
        u.setPassword(password);
        u.setNickname(nickname);
        u.setId(id);
        u.setUserType(UserTypeEnum.NORMAL);
        u.setStatus(AccountStatusEnum.NORMAL);
        save(u);


    }

    @Override
    public String login(String username, String password) {
        if(username==null || password==null){
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        User user = lambdaQuery().eq(User::getUsername, username).one();
        if(user==null){
            return null;
        }
        String pwd = user.getPassword();
        //匹配则生成jwt令牌
        if(!passwordEncoder.matches(password,pwd)){
            return null;
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = jwtUtil.generateToken(claims);
        if(token==null){
            throw new BusinessException("token生成失败");
        }
        return token;
    }
}
