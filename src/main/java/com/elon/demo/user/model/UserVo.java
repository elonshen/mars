package com.elon.demo.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class UserVo {
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 用户名
     */
    private String username;
    /**
     * 角色
     */
    private Set<Role> roles = new LinkedHashSet<>();
    @Schema(description = "创建时间,ISO-8601标准表示，不带时区，例如：2007-12-03T10:15:30")
    private LocalDateTime createdTime;

    public UserVo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
