package com.elon.mars.controller;

import com.elon.mars.domain.Tenant;
import com.elon.mars.domain.User;
import com.elon.mars.repository.TenantRepository;
import com.elon.mars.repository.UserRepository;
import com.elon.mars.service.JWTService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "认证")
@Hidden
public class AuthenticationController {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JWTService jwtService;

    public AuthenticationController(UserRepository userRepository, TenantRepository tenantRepository, JWTService jwtService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.jwtService = jwtService;
    }

    /**
     * 用户登入
     *
     * @return token
     */
    @PostMapping("/authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(examples = @ExampleObject(value = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTYzNTc3NjQ2NywiaWF0IjoxNjM0NDgwNDY3fQ.FG1jp0mQodtslGfSHShrgo2DOkKQcj_pCvLRe5Q5t3w"))}),
    })
    public String createAuthenticationToken(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findFirstByAuth_Username(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new RuntimeException("租户不存在"));

        return jwtService.generateToken(authentication, user, tenant);
    }

    @GetMapping("/")
    public String hello(Authentication authentication) {
        return "Hello, " + authentication.getName() + "!";
    }
}
