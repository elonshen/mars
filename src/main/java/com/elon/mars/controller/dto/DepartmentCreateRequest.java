package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * 部门创建请求
 *
 * @param name     部门名称
 * @param parentId 父部门ID（可选）
 * @param userIds  关联的用户ID列表（可选）
 */
public record DepartmentCreateRequest(
        @NotBlank(message = "部门名称不能为空")
        String name,
        Long parentId,
        Set<Long> userIds
) {
}
