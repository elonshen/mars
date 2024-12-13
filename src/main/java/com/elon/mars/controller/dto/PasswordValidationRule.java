package com.elon.mars.controller.dto;

import java.util.Set;

public record PasswordValidationRule(
        int minLength,
        int maxLength,
        boolean requireUpperCase,
        boolean requireLowerCase,
        boolean requireDigit,
        boolean requireSpecialChar,
        Set<String> forbiddenWords,
        int maxRepeatedChars
) {
    public static PasswordValidationRule DEFAULT = new PasswordValidationRule(
            8,        // 最小长度
            20,       // 最大长度
            true,     // 必须包含大写字母
            true,     // 必须包含小写字母
            true,     // 必须包含数字
            true,     // 必须包含特殊字符
            Set.of("password", "123456", "admin", "test"), // 禁用密码
            3        // 最大连续重复字符数
    );
}