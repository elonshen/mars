package com.elon.mars.repository;

import com.elon.mars.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    /*跨租户操作*/
    void deleteByTenantId(Long tenantId);

    /*租户操作*/
    boolean existsByCode(String code);

}