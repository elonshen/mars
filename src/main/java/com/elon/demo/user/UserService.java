package com.elon.demo.user;

import com.elon.demo.user.model.User;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(@NonNull String username);

    User save(User user);
}
