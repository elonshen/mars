package com.elon.demo.user.model;

import jakarta.validation.constraints.NotBlank;


public class UserUpdatePasswordRequest {
    /**
     * 密码
     */
    @NotBlank
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
