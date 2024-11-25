package com.elon.mars.service;

import com.elon.mars.domain.Tenant;
import com.elon.mars.domain.TenantType;
import com.elon.mars.domain.User;
import com.elon.mars.repository.TenantRepository;
import com.elon.mars.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public SecurityService(UserRepository userRepository, TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    // 获取基础信息的静态方法
    public static Long getCurrentUserId() {
        return getJwtClaim("userId");
    }

    public static Long getCurrentTenantId() {
        return getJwtClaim("tenantId");
    }

    public static TenantType getCurrentTenantType() {
        String type = getJwtClaim("tenantType");
        return TenantType.valueOf(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getJwtClaim(String claimName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return (T) jwt.getClaims().get(claimName);
        }
        throw new RuntimeException("Unable to get " + claimName);
    }

    // 租户类型判断方法
    public static boolean isPlatformTenant() {
        return getCurrentTenantType() == TenantType.PLATFORM;
    }

    public static boolean isRegulatoryTenant() {
        return getCurrentTenantType() == TenantType.REGULATORY;
    }

    public static boolean isNormalTenant() {
        return getCurrentTenantType() == TenantType.NORMAL;
    }

    // 获取实体对象的实例方法
    public User getCurrentUser() {
        return userRepository.findById(getCurrentUserId()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Tenant getCurrentTenant() {
        return tenantRepository.findById(getCurrentTenantId()).orElseThrow(() -> new RuntimeException("Tenant not found"));
    }
}