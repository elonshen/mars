package com.elon.demo.user;

import com.elon.demo.user.model.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Tag(name = "用户管理")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 新增用户
     */
    @PostMapping
    public void create(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        if (userRepository.findByUsername(userCreateRequest.getUsername()).isPresent()){
            throw new RuntimeException("用户名已存在");
        }

        User user = userMapper.toUser(userCreateRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    /**
     * 获取当前登陆的用户信息
     */
    @GetMapping("/me")
    public UserVO getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.toUserVo(userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("该用户不存在")));
    }

    /**
     * 获取用户列表分页信息
     *
     * @param startCreateTime A date-time without a time-zone in the ISO-8601 calendar system, such as 2007-12-03T10:15:30
     * @param endCreateTime   A date-time without a time-zone in the ISO-8601 calendar system, such as 2007-12-03T10:15:30
     */
    @GetMapping()
    public Page<UserVO> getUserInfo(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startCreateTime,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endCreateTime,
                                    @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
            if (name != null) {
                predicatesList.add(criteriaBuilder.equal(root.get("name"), name));
            }
            if (startCreateTime != null && endCreateTime != null) {
                predicatesList.add(criteriaBuilder.between(root.get("createTime"), startCreateTime, endCreateTime));
            } else if (startCreateTime == null && endCreateTime != null) {
                predicatesList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), endCreateTime));
            } else if (startCreateTime != null) {
                predicatesList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startCreateTime));
            }
            Predicate[] predicates = new Predicate[predicatesList.size()];
            return criteriaBuilder.and(predicatesList.toArray(predicates));
        };

        Page<User> userPage = userRepository.findAll(specification, pageable);
        return userMapper.toUserVoPage(userPage.getContent(), pageable, userPage.getTotalElements());
    }

    /**
     * 删除用户
     * @param id 用户ID
     */
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param userUpdateRequest 用户信息
     */
    @PutMapping("/{id}")
    public void updateUserInfo(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        if (userRepository.existsByUsernameAndIdNot(userUpdateRequest.getUsername(),id)){
            throw new RuntimeException("用户名已存在");
        }
        User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户ID不存在"));
        userMapper.updatePerson(userUpdateRequest, targetUser);
        userRepository.save(targetUser);
    }

    /**
     * 更新用户密码
     *
     * @param id                        用户ID
     * @param userUpdatePasswordRequest 密码
     */
    @PutMapping("/{id}/password")
    public void updateUserPassword(@PathVariable Long id, @RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest) {
        User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户ID不存在"));
        targetUser.setPassword(passwordEncoder.encode(userUpdatePasswordRequest.getPassword()));
        userRepository.save(targetUser);
    }
}
