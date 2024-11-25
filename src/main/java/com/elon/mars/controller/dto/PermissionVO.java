package com.elon.mars.controller.dto;

/**
 * 权限信息展示对象
 *
 * @param id   权限ID
 * @param name 权限名称
 * @param code 权限代码
 */
public record PermissionVO(
        Long id,
        String name,
        String code
) {
}