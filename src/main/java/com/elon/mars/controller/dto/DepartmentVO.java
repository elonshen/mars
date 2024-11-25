package com.elon.mars.controller.dto;

import java.util.Set;

/**
 * 部门信息展示对象
 *
 * @param id       部门ID
 * @param name     部门名称
 * @param parentId 父部门ID
 * @param userIds  关联的用户ID列表
 */
public record DepartmentVO(
        Long id,
        String name,
        Long parentId,
        Set<Long> userIds
) {
}
