package com.elon.demo.user;

import com.elon.demo.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(@NonNull String username);

    User create(User user);

    User update(User user);

    Page<User> findByNameContaining(String name, Pageable pageable);

    void deleteById(Long id);
}
