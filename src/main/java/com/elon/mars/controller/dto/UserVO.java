package com.elon.mars.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户信息展示对象
 *
 * @param id            用户ID
 * @param name          用户名称
 * @param username      登录用户名
 * @param roleIds       关联的角色ID列表
 * @param roleNames     关联的角色名称列表
 * @param departmentIds 关联的部门ID列表
 * @param departmentNames 关联的部门名称列表
 * @param createdTime   创建时间
 * @param permissionCodes 权限码列表
 */
public record UserVO(
        Long id,
        String name,
        String username,
        Set<Long> roleIds,
        Set<String> roleNames,
        Set<Long> departmentIds,
        Set<String> departmentNames,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdTime,
        Set<String> permissionCodes
) {}
