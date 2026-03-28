package com.elon.mars.domain;

public enum PermissionEnum {
    USER_MANAGE("用户管理"),
    TENANT_MANAGE("租户管理"),
    PERMISSION_MANAGE("权限管理"),
    ROLE_MANAGE("角色管理"),
    DEPARTMENT_MANAGE("部门管理");

    private final String name;

    PermissionEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}