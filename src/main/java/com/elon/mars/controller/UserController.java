package com.elon.mars.controller;

import com.elon.mars.controller.dto.UserCreateRequest;
import com.elon.mars.controller.dto.UserUpdatePasswordRequest;
import com.elon.mars.controller.dto.UserUpdateRequest;
import com.elon.mars.controller.dto.UserVO;
import com.elon.mars.controller.mapper.UserMapper;
import com.elon.mars.domain.Role;
import com.elon.mars.domain.User;
import com.elon.mars.repository.RoleRepository;
import com.elon.mars.repository.UserRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/users")
@Tag(name = "用户管理")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    /**
     * 新增用户
     */
    @PostMapping
    public void create(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        if (userRepository.findFirstByAuth_Username(userCreateRequest.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(userCreateRequest.getRoleIds()));

        User user = userMapper.toUser(userCreateRequest);
        user.setRoles(roles);
        user.getAuth().setPassword(userCreateRequest.getPassword());
        userRepository.save(user);
    }

    /**
     * 获取当前登陆的用户信息
     */
    @GetMapping("/me")
    public UserVO getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userMapper.toUserVo(userRepository.findFirstByAuth_Username(authentication.getName()).orElseThrow(() -> new RuntimeException("该用户不存在")));
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
        if (userRepository.existsByAuth_UsernameAndIdNot(userUpdateRequest.getUsername(), id)) {
            throw new RuntimeException("用户名已存在");
        }
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(userUpdateRequest.getRoleIds()));
        User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户ID不存在"));
        userMapper.updatePerson(userUpdateRequest, targetUser);
        targetUser.setRoles(roles);
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
        targetUser.getAuth().setPassword(passwordEncoder.encode(userUpdatePasswordRequest.getPassword()));
        userRepository.save(targetUser);
    }
}
