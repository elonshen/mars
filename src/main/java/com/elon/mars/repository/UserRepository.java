package com.elon.mars.repository;

import com.elon.mars.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findFirstByAuth_Username(String username);

    boolean existsByAuth_UsernameAndIdNot(String username, Long id);
}