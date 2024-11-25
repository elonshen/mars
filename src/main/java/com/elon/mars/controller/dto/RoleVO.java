package com.elon.mars.controller.dto;

import java.util.Set;

/**
 * 角色信息展示对象
 *
 * @param id            角色ID
 * @param name          角色名称
 * @param permissionIds 关联的权限ID列表
 */
public record RoleVO(
        Long id,
        String name,
        Set<Long> permissionIds
) {
}