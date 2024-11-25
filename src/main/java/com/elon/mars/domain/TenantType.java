package com.elon.mars.domain;

public enum TenantType {
    /**
     * 平台
     */
    PLATFORM,
    /**
     * 租户
     */
    NORMAL,
    /**
     * 监管租户
     */
    REGULATORY;

    /**
     * 字符串转枚举
     *
     * @param name 枚举名称
     * @return com.elon.mars.domain.TenantType
     */
    public static TenantType of(String name) {
        for (TenantType value : TenantType.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
