package com.elon.demo.user.model;

import jakarta.validation.constraints.NotBlank;

import java.util.LinkedHashSet;
import java.util.Set;


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
    private Set<Role> roles = new LinkedHashSet<>();
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
