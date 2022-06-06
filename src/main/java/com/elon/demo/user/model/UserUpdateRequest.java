package com.elon.demo.user.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public class UserUpdateRequest {
    @NotNull
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    private String password;
    /**
     * admin
     */
    private String role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
