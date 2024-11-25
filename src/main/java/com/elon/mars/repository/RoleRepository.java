package com.elon.mars.repository;

import com.elon.mars.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    /* 跨租户操作 */
    List<Role> findByTenantId(Long tenantId);

    void deleteByTenantId(Long tenantId);


    /* 租户操作 */
}