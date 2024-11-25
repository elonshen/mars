package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * 角色创建请求
 *
 * @param name          角色名称
 * @param permissionIds 关联的权限ID列表
 */
public record RoleCreateRequest(
        @NotBlank(message = "角色名称不能为空")
        String name,
        Set<Long> permissionIds
) {
}
