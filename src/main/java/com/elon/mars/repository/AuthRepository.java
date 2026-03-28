package com.elon.mars.repository;

import com.elon.mars.domain.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthRepository extends JpaRepository<Auth, Long> {
    @Query(value = "SELECT * FROM auth WHERE username = :username", nativeQuery = true)
    Auth findByUsernameNative(@Param("username") String username);
}