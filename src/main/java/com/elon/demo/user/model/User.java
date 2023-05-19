package com.elon.demo.user.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Table(name = "user")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    /**
     * 名称
     */
    @Column(name = "name")
    private String name;
    /**
     * 用户名
     */
    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @CreatedDate
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    /**
     * 角色
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "role")
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "owner_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new LinkedHashSet<>();

    @SuppressWarnings("unused")
    public User(User user) {
        this.id = user.id;
        this.name = user.name;
        this.username = user.username;
        this.password = user.password;
        this.roles = user.roles;
        this.createdTime = user.createdTime;
    }

    private User(Long id, String name, String username, String password, LocalDateTime createdTime, Set<Role> roles) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.createdTime = createdTime;
        this.roles = roles;
    }

    public static User ofNew(String name, String username, String password, Set<Role> roles) {
        return new User(null, name, username, new BCryptPasswordEncoder().encode(password), null, roles);
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public User() {
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createTime) {
        this.createdTime = createTime;
    }
}