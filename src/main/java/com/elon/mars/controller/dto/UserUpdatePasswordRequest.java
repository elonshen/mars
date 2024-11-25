package com.elon.mars.controller.dto;

import jakarta.validation.constraints.NotBlank;


/**
 * 用户密码更新请求
 *
 * @param password 新密码
 */
public record UserUpdatePasswordRequest(@NotBlank String password) {
}
