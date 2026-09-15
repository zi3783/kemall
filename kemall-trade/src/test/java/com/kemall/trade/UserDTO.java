package com.kemall.trade;

import lombok.Data;
import lombok.ToString;

// com.kemall.user.dto.com.kemall.trade.UserDTO
@Data
@ToString
public class UserDTO {
    private Long id;
    private String username;
    private Integer age;
}