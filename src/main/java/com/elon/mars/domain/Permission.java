package com.elon.mars.domain;

import com.elon.mars.config.SnowflakeGenerator;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TenantId;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "permission", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_permission_code_tenant",
                columnNames = {"code", "tenant_id"}
        )
})
public class Permission {
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", type = SnowflakeGenerator.class)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "tenant_id")
    @TenantId
    private Long tenantId;

    /**
     * 静态构造方法,不带租户信息
     */
    public static Permission of(String name, String code) {
        return Permission.of(name, code, null);
    }

    /**
     * 静态构造方法
     */
    public static Permission of(String name, String code, Long tenantId) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setCode(code);
        permission.setTenantId(tenantId);
        return permission;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenant) {
        this.tenantId = tenant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Permission that = (Permission) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}