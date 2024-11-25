package com.elon.mars.repository;

import com.elon.mars.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /* 跨租户操作 */
    @Query(value = "SELECT u.* FROM user u INNER JOIN auth a ON u.auth_id = a.id WHERE a.id = :authId AND u.tenant_id = :tenantId", nativeQuery = true)
    Optional<User> findByAuthIdAndTenantIdNative(@Param("authId") Long authId, @Param("tenantId") Long tenantId);

    List<User> findAllByTenantId(Long tenantId);

    @Query(value = "select u.* from user u inner join auth a on u.auth_id = a.id where a.id = :authId", nativeQuery = true)
    List<User> findByAuth_IdNative(Long authId);

    long countByTenantId(Long tenantId);

    void deleteByTenantId(Long tenantId);


    /* 租户操作 */
    Optional<User> findFirstByAuth_Username(String username);

    Optional<User> findByAuth_Username(String username);

    boolean existsByAuth_UsernameAndIdNot(String username, Long id);
}