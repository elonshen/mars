package com.elon.mars.controller;

import com.elon.mars.domain.Tenant;
import com.elon.mars.domain.User;
import com.elon.mars.repository.TenantRepository;
import com.elon.mars.repository.UserRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "认证")
@Hidden
public class AuthenticationController {
    private final JwtEncoder encoder;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public AuthenticationController(JwtEncoder encoder, UserRepository userRepository, TenantRepository tenantRepository) {
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
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
        Optional<User> userOptional = userRepository.findFirstByAuth_Username(username); //默认登录第一个租户
        User user = userOptional.orElseThrow(() -> new RuntimeException("用户不存在"));

        Long tenantId = user.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new RuntimeException("租户不存在"));

        Instant now = Instant.now();
        long expiry = 36000L;
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(username)
                .claim("scope", scope)
                .claim("userId", user.getId())
                .claim("tenantId", tenantId)
                .claim("tenantType", tenant.getTenantType().name())
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @GetMapping("/")
    public String hello(Authentication authentication) {
        return "Hello, " + authentication.getName() + "!";
    }
}
