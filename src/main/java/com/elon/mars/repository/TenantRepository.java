package com.elon.mars.repository;

import com.elon.mars.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant> {
    boolean existsByName(String name);

    // 查询指定ID的租户
    @Query(value = "SELECT t.* FROM tenant t WHERE t.id = :id", nativeQuery = true)
    Optional<Tenant> findByIdNative(@Param("id") Long id);

}