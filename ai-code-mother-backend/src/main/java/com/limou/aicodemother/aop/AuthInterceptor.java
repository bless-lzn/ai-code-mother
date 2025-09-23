package com.limou.aicodemother.aop;

import com.limou.aicodemother.annotation.AuthCheck;
import com.limou.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(JoinPoint joinPoint, AuthCheck authCheck){

    }

}
