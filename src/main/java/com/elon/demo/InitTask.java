package com.elon.demo;

import com.elon.demo.user.UserRepository;
import com.elon.demo.user.model.Role;
import com.elon.demo.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.List;

@Component
public class InitTask implements CommandLineRunner {
    private final UserRepository userRepository;
    private final String inspectionPhotoPath;

    public InitTask(UserRepository userRepository, @Value("${mars.inspection.photo-path}") String inspectionPhotoPath) {
        this.userRepository = userRepository;
        this.inspectionPhotoPath = inspectionPhotoPath;
    }

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {
            // 初始化管理员用户
            User user = User.ofNew("admin", "admin", "admin123", new HashSet<>(List.of(Role.ADMIN)));
            userRepository.save(user);
        }

        File file1 = new File(inspectionPhotoPath);
        if (!file1.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file1.mkdir();
        }
    }
}
