package com.elon.mars.service;

import com.elon.mars.domain.Permission;
import com.elon.mars.domain.Tenant;
import com.elon.mars.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class JWTService {
    private final JwtEncoder jwtEncoder;
    private static final long TOKEN_EXPIRY_SECONDS = 36000L;

    public JWTService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * 生成JWT token
     * @param user 用户信息
     * @param tenant 租户信息
     * @return JWT token字符串
     */
    public String generateToken(User user, Tenant tenant) {
        String scope = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .collect(Collectors.joining(" "));

        return generateToken(user.getAuth().getUsername(), scope, user.getId(), tenant.getId(), tenant.getTenantType().name());
    }

    /**
     * 基于Authentication生成JWT token
     * @param authentication Spring Security认证信息
     * @param user 用户信息
     * @param tenant 租户信息
     * @return JWT token字符串
     */
    public String generateToken(Authentication authentication, User user, Tenant tenant) {
        String scope = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(" "));

        return generateToken(authentication.getName(), scope, user.getId(), tenant.getId(), tenant.getTenantType().name());
    }

    /**
     * 生成JWT token的核心方法
     * @param subject 主题(通常是用户名)
     * @param scope 权限范围
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @param tenantType 租户类型
     * @return JWT token字符串
     */
    private String generateToken(String subject, String scope, Long userId, Long tenantId, String tenantType) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_EXPIRY_SECONDS))
                .subject(subject)
                .claim("scope", scope)
                .claim("userId", userId)
                .claim("tenantId", tenantId)
                .claim("tenantType", tenantType)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}