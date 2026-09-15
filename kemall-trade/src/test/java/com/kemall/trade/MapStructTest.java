package com.kemall.trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TradeApplication.class)
public class MapStructTest {

    @Autowired
    private UserMapper userMapper;

    @org.junit.jupiter.api.Test
    public void Main(){
        UserDTO userDTO = getUserDTO();
        System.out.println(userDTO);
    }

    public UserDTO getUserDTO() {
        User user = new User();
        user.setId(1L);
        user.setAge(18);
        user.setPassword("123456");
        user.setUsername("admin");
        return userMapper.toTarget(user);
    }
}
