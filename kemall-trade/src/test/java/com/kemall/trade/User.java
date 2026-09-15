package com.kemall.trade;

import lombok.Data;

// com.kemall.user.entity.com.kemall.trade.User
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private Integer age;
}