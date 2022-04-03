package com.elon.demo.user;

import com.elon.demo.authentication.model.MyUserDetails;
import com.elon.demo.user.model.User;
import com.elon.demo.user.model.UserCreateRequest;
import com.elon.demo.user.model.UserUpdateRequest;
import com.elon.demo.user.model.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Tag(name = "user")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public void save(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        User user = userMapper.toUser(userCreateRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @GetMapping("/current")
    @Operation(summary = "get the current login user information")
    public UserVo getUserInfo() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userMapper.toUserVo(userDetails);
    }

    @GetMapping
    @Operation(summary = "get user paging information")
    public Page<UserVo> getUserInfo(@RequestParam(required = false) String name, @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<User> userPage;
        if (name != null) {
            userPage = userRepository.findByNameContaining(name, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return userMapper.toUserVoPage(userPage.getContent(), pageable, userPage.getTotalElements());
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @PutMapping
    public void save(@RequestBody @Valid UserUpdateRequest userCreateRequest) {
        User user = userMapper.toUser(userCreateRequest);
        if (StringUtils.isBlank(user.getPassword())) {
            Long userId = user.getId();
            User sourceUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("userId " + userId + " is not exit"));
            user.setPassword(sourceUser.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
    }
}
