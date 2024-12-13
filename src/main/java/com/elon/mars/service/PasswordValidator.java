package com.elon.mars.service;

import com.elon.mars.controller.dto.PasswordValidationRule;

public class PasswordValidator {
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    /**
     * 校验密码强度
     *
     * @param password 待校验的密码
     * @param username 用户名，用于检查密码是否包含用户名
     * @param rule     校验规则
     * @throws RuntimeException 当密码不符合规则时抛出对应错误信息
     */
    public static void validate(String password, String username, PasswordValidationRule rule) {
        // 长度校验
        if (password.length() < rule.minLength()) {
            throw new RuntimeException("密码长度不能小于" + rule.minLength() + "位");
        }
        if (password.length() > rule.maxLength()) {
            throw new RuntimeException("密码长度不能大于" + rule.maxLength() + "位");
        }

        // 密码复杂度校验
        if (rule.requireUpperCase() && !password.matches(".*[A-Z].*")) {
            throw new RuntimeException("密码必须包含大写字母");
        }
        if (rule.requireLowerCase() && !password.matches(".*[a-z].*")) {
            throw new RuntimeException("密码必须包含小写字母");
        }
        if (rule.requireDigit() && !password.matches(".*\\d.*")) {
            throw new RuntimeException("密码必须包含数字");
        }
        if (rule.requireSpecialChar() && !containsSpecialChar(password)) {
            throw new RuntimeException("密码必须包含特殊字符");
        }

        // 禁用密码校验
        String lowerPassword = password.toLowerCase();
        if (rule.forbiddenWords().stream().anyMatch(lowerPassword::contains)) {
            throw new RuntimeException("密码不能包含常见密码组合");
        }

        // 用户名相关校验
        if (username != null && !username.isEmpty()) {
            if (lowerPassword.contains(username.toLowerCase())) {
                throw new RuntimeException("密码不能包含用户名");
            }
        }

        // 连续重复字符校验
        char[] chars = password.toCharArray();
        int repeatCount = 1;
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == chars[i - 1]) {
                repeatCount++;
                if (repeatCount > rule.maxRepeatedChars()) {
                    throw new RuntimeException("密码不能包含" + rule.maxRepeatedChars() + "个以上连续重复的字符");
                }
            } else {
                repeatCount = 1;
            }
        }
    }

    private static boolean containsSpecialChar(String password) {
        return password.chars().anyMatch(ch -> SPECIAL_CHARS.indexOf(ch) >= 0);
    }
}