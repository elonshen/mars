package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;


/**
 * 用户信息更新请求
 *
 * @param name          用户名称
 * @param username      登录用户名
 * @param roleIds       关联的角色ID列表
 * @param departmentIds 关联的部门ID列表
 */
public record UserUpdateRequest(
        @NotBlank String name,
        @NotBlank String username,
        Set<Long> roleIds,
        Set<Long> departmentIds
) {
}
