package com.elon.mars.domain;

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

    @CreatedDate
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 角色
     */
    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "auth_id", nullable = false)
    private Auth auth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = true)
    private Tenant tenant;

    public static User ofNew(String name, String username, String password, Set<Role> roles, Tenant tenant) {
        User user = new User();
        user.setName(name);
        Auth auth = new Auth();
        auth.setUsername(username);
        auth.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setAuth(auth);
        user.setRoles(roles);
        user.setTenant(tenant);
        return user;
    }

    public static User ofNew(String name, String username, String password, Set<Role> roles) {
        return ofNew(name, username, password, roles, null);
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public User(Long id) {
        this.id = id;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public User() {
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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createTime) {
        this.createdTime = createTime;
    }
}