package com.elon.demo.user;

import com.elon.demo.user.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        if (StringUtils.isBlank(user.getPassword())) {
            Long userId = user.getId();
            User sourceUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("userId " + userId + " is not exit"));
            user.setPassword(sourceUser.getPassword());
        }
        return userRepository.save(user);
    }

    @Override
    public Page<User> findByNameContaining(String name, Pageable pageable) {
        if (name != null) {
            return userRepository.findByNameContaining(name, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
