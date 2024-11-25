package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建租户的请求
 *
 * @param name        租户名称
 * @param description 租户描述(可选)
 * @param tenantType  租户类型创建请求:NORMAL:普通租户,REGULATORY:监管租户
 * @param adminName   租户管理员显示名称
 */
public record TenantCreateRequest(
        @NotBlank(message = "租户名称不能为空")
        String name,

        String description,

        @NotNull(message = "租户类型不能为空")
        TenantTypeCreateRequest tenantType,

        @NotBlank(message = "管理员显示名称不能为空")
        String adminName
) {
}