package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;


/**
 * 用户创建请求
 *
 * @param name          用户名称
 * @param username      登录用户名
 * @param password      登录密码
 * @param roleIds       关联的角色ID列表
 * @param departmentIds 关联的部门ID列表
 */
public record UserCreateRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank String password,
        Set<Long> roleIds,
        Set<Long> departmentIds
) {
}