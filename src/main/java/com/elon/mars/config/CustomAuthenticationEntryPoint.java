package com.elon.mars.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("WWW-Authenticate", "None");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = "认证失败";

        if (authException instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        } else if (authException instanceof DisabledException) {
            message = "账户已被禁用";
        } else if (authException instanceof AccountExpiredException) {
            message = "账户已过期";
        } else if (authException instanceof LockedException) {
            message = "账户已被锁定";
        } else if (authException instanceof InvalidBearerTokenException && authException.getMessage().contains("expired")) {
            message = "认证已过期，请重新登录";
        } else {
            // 记录详细信息
            logger.warn("其他认证失败", authException);
        }

        ErrorResponse errorResponse = new ErrorResponse(message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}