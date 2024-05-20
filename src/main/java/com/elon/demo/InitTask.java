package com.elon.demo;

import com.elon.demo.user.UserRepository;
import com.elon.demo.user.model.Role;
import com.elon.demo.user.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class InitTask implements CommandLineRunner {
    private final UserRepository userRepository;

    public InitTask(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {
            // 初始化管理员用户
            User user = User.ofNew("admin", "admin", "admin123", new HashSet<>(List.of(Role.ADMIN)));
            userRepository.save(user);
        }
    }
}
