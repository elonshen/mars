package com.elon.demo.config;


import com.elon.demo.user.UserRepository;
import com.elon.demo.user.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class SpringDataConfig {

    private final UserRepository userRepository;

    public SpringDataConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    AuditorAware<User> auditorProvider() {
        return () -> {
            User user;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("该用户不存在"));
            } else {
                user = null;
            }
            return Optional.ofNullable(user);
        };
    }
}
