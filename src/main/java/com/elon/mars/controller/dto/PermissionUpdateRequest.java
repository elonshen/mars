package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 权限更新请求
 *
 * @param name 权限名称
 */
public record PermissionUpdateRequest(
        @NotBlank(message = "权限名称不能为空")
        String name
) {
}
