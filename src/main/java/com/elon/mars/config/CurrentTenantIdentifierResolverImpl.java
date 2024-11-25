package com.elon.mars.config;

import com.elon.mars.service.SecurityService;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<Long>, HibernatePropertiesCustomizer {

    /**
     * 如果没有认证用户则默认为平台租户,否则取认证用户的租户ID
     *
     * @return 租户ID
     */
    @Override
    public Long resolveCurrentTenantIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return 114166601818112L;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            // JWT认证
            return (Long) jwt.getClaims().get("tenantId");
        } else if (principal instanceof UserDetails) {
            // Basic认证
            return 114166601818112L;  // 使用默认平台租户
        } else if ("anonymousUser".equals(principal)) {
            return 114166601818112L;  // 匿名用户使用默认平台租户
        }

        throw new RuntimeException("无法获取租户ID");
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

    /**
     * 是否是Root租户
     * <p>如果是Root租户,则可以访问所有租户的数据,否则只能访问自己的租户数据.</p>
     * <p>root租户在查询时不会添加租户ID条件.保存时,如果实体有租户ID则会保存实体的租户ID,如果没有则根据{@link #resolveCurrentTenantIdentifier()}获取的租户ID保存</p>
     * <p>如果是普通租户,查询时会自动添加租户ID条件,保存时,无论实体是否带有租户ID,该租户ID都会使用{@link #resolveCurrentTenantIdentifier()}的租户ID</p>
     *
     * @param tenantId a tenant id produced by {@link #resolveCurrentTenantIdentifier()}
     * @return 是否是Root租户
     */
    @Override
    public boolean isRoot(Long tenantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() instanceof UserDetails || "anonymousUser".equals(authentication.getPrincipal())) {
            return true;
        }

        return SecurityService.isPlatformTenant() || SecurityService.isRegulatoryTenant();
    }
}