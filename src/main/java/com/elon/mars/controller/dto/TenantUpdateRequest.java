package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新租户的请求
 *
 * @param name        租户名称
 * @param description 租户描述(可选)
 */
public record TenantUpdateRequest(
        @NotBlank(message = "租户名称不能为空")
        String name,

        String description
) {
}