package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 权限创建请求
 *
 * @param name 权限名称
 * @param code 权限代码
 */
public record PermissionCreateRequest(
        @NotBlank(message = "权限名称不能为空")
        String name,

        @NotBlank(message = "权限代码不能为空")
        String code
) {
}
