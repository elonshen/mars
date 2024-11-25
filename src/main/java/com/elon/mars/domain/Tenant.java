package com.elon.mars.domain;

import com.elon.mars.config.SnowflakeGenerator;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "tenant")
public class Tenant {
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", type = SnowflakeGenerator.class)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type", nullable = false)
    private TenantType tenantType;

    /**
     * 静态构造方法
     */
    public static Tenant of(String name, String description, TenantType tenantType) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setDescription(description);
        tenant.setTenantType(tenantType);
        return tenant;
    }

    public TenantType getTenantType() {
        return tenantType;
    }

    public void setTenantType(TenantType tenantType) {
        this.tenantType = tenantType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}