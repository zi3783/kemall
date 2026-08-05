package com.kemall.user.domain.dto;


import lombok.Data;

@Data
public class UserDTO {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
