package com.kemall.user.controller;


import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.Result;
import com.kemall.common.utils.UserContext;
import com.kemall.user.domain.dto.UserDTO;
import com.kemall.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-08-05
 */
@Tag(name = "用户相关接口")
@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping
    @Operation(summary = "用户注册")
    @LoginRequire(login = false)
    public void registerUser(@RequestBody UserDTO user) {
        userService.registerUser(user);
    }

    @GetMapping("/login")
    @Operation(summary = "用户登录")
    @LoginRequire(login = false)
    public Result<String> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        String token = userService.login(username, password);
        if(token==null){
            return Result.fail("用户不存在或密码错误");
        }
        return Result.success(token);
    }

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("用户id：" + UserContext.getUserId());
    }


}
