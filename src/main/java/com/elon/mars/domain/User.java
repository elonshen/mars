package com.elon.mars.domain;

import com.elon.mars.config.SnowflakeGenerator;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TenantId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Table(name = "`user`")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", type = SnowflakeGenerator.class)
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

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "user_department",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    private Set<Department> departments = new LinkedHashSet<>();

    @Column(name = "tenant_id")
    @TenantId
    private Long tenantId;

    public static User ofNew(String name, String username, String password, Set<Role> roles, Set<Department> departments, Long tenant) {
        User user = new User();
        user.setName(name);
        Auth auth = new Auth();
        auth.setUsername(username);
        auth.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setAuth(auth);
        user.setRoles(roles);
        user.setTenantId(tenant);
        user.setDepartments(departments);
        return user;
    }

    public static User ofNew(String name, String username, String password, Set<Role> roles, Set<Department> departments) {
        return ofNew(name, username, password, roles, departments, null);
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenant) {
        this.tenantId = tenant;
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