package com.elon.mars.controller.dto;

import com.elon.mars.domain.TenantType;

/**
 * 租户信息响应
 *
 * @param id          租户ID
 * @param name        租户名称
 * @param description 租户描述
 * @param tenantType  租户类型, 参考 {@link TenantType}
 */
public record TenantVO(
        Long id,
        String name,
        String description,
        TenantType tenantType
) {
}
