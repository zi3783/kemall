package com.kemall.common.interceptor;

import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.properties.AuthProperties;
import com.kemall.common.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final AuthProperties authProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        //判断是否为静态资源
        if(!(handler instanceof HandlerMethod)){
            log.info("静态资源放行");
            return true;
        }
        //注解优先级更高
        Method method = ((HandlerMethod) handler).getMethod();
        LoginRequire annotation = method.getAnnotation(LoginRequire.class);
        if(annotation != null && annotation.login() == false){
            log.info("有@LoginRequire注解放行");
            //不需要登录
            return true;
        }
        //无注解再根据配置文件筛选
        String url = request.getRequestURI();
        if(isWhiteList(url)){
            log.info("白名单放行");
            //白名单直接放行
            return true;
        }

        //需要登录
        //取出userId
        String uid = request.getHeader("userId");
        if(uid == null){
            //没有找到userId直接拦截
            log.warn("请求头没有userId，拦截");
            return false;
        }
        Long userId = Long.valueOf(uid);

        //放入ThreadLocal
        UserContext.setUserId(userId);
        return true;
    }

    //不支持/**通配符
    private boolean isWhiteList(String url) {
        List<String> whiteList = authProperties.getWhiteList();
        if(whiteList == null || whiteList.isEmpty()){
            return false;
        }
        return whiteList.contains(url);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.removeUserId();
    }
}
