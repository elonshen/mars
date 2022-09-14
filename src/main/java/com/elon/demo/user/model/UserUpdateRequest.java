package com.elon.demo.user.model;

import javax.validation.constraints.NotBlank;


public class UserUpdateRequest {
    /**
     * 名称
     */
    @NotBlank
    private String name;
    /**
     * 用户名
     */
    @NotBlank
    private String username;
    /**
     * 角色
     */
    private String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
