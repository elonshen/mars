package com.elon.mars.controller;

import com.elon.mars.controller.dto.*;
import com.elon.mars.controller.mapper.UserMapper;
import com.elon.mars.domain.Department;
import com.elon.mars.domain.Role;
import com.elon.mars.domain.User;
import com.elon.mars.repository.DepartmentRepository;
import com.elon.mars.repository.RoleRepository;
import com.elon.mars.repository.UserRepository;
import com.elon.mars.service.PasswordValidator;
import com.elon.mars.service.SecurityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/users")
@Tag(name = "用户资源")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final SecurityService securityService;
    private final DepartmentRepository departmentRepository;

    public UserController(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository, SecurityService securityService, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.securityService = securityService;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 新增用户
     */
    @PostMapping
    public void create(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        if (userRepository.findByAuth_Username(userCreateRequest.username()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 密码强度校验
        PasswordValidator.validate(
                userCreateRequest.password(),
                userCreateRequest.username(),
                PasswordValidationRule.DEFAULT
        );

        User user = userMapper.toUser(userCreateRequest);

        // 设置角色
        if (userCreateRequest.roleIds() != null && !userCreateRequest.roleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(userCreateRequest.roleIds()));
            user.setRoles(roles);
        }

        // 设置部门
        if (userCreateRequest.departmentIds() != null && !userCreateRequest.departmentIds().isEmpty()) {
            Set<Department> departments = new HashSet<>(departmentRepository.findAllById(userCreateRequest.departmentIds()));
            user.setDepartments(departments);
        }

        user.getAuth().setPassword(new BCryptPasswordEncoder().encode(userCreateRequest.password()));
        userRepository.save(user);
    }

    /**
     * 获取当前登陆的用户信息
     */
    @GetMapping("/me")
    public UserVO getUserInfo() {
        return userMapper.toUserVo(securityService.getCurrentUser());
    }

    /**
     * 获取用户列表分页信息
     *
     * @param name 用户名称,可选
     * @param departmentId 部门ID,可选
     * @param startCreateTime 创建开始时间,可选,格式如:2007-12-03T10:15:30
     * @param endCreateTime 创建结束时间,可选,格式如:2007-12-03T10:15:30
     */
    @GetMapping
    public Page<UserVO> getUserInfo(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startCreateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endCreateTime,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();

            // 租户过滤
            predicatesList.add(criteriaBuilder.equal(root.get("tenantId"), SecurityService.getCurrentTenantId()));

            // 名称过滤
            if (name != null) {
                predicatesList.add(criteriaBuilder.equal(root.get("name"), name));
            }

            // 部门过滤
            if (departmentId != null) {
                Join<User, Department> departmentJoin = root.join("departments", JoinType.INNER);
                predicatesList.add(criteriaBuilder.equal(departmentJoin.get("id"), departmentId));
            }

            // 创建时间过滤
            if (startCreateTime != null && endCreateTime != null) {
                predicatesList.add(criteriaBuilder.between(root.get("createTime"), startCreateTime, endCreateTime));
            } else if (startCreateTime == null && endCreateTime != null) {
                predicatesList.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), endCreateTime));
            } else if (startCreateTime != null) {
                predicatesList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startCreateTime));
            }

            return criteriaBuilder.and(predicatesList.toArray(new Predicate[0]));
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
        if (userRepository.existsByAuth_UsernameAndIdNot(userUpdateRequest.username(), id)) {
            throw new RuntimeException("用户名已存在");
        }

        User targetUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户ID不存在"));
        userMapper.updatePerson(userUpdateRequest, targetUser);

        // 更新角色
        if (userUpdateRequest.roleIds() != null && !userUpdateRequest.roleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(userUpdateRequest.roleIds()));
            targetUser.setRoles(roles);
        }

        // 更新部门
        if (userUpdateRequest.departmentIds() != null && !userUpdateRequest.departmentIds().isEmpty()) {
            Set<Department> departments = new HashSet<>(departmentRepository.findAllById(userUpdateRequest.departmentIds()));
            targetUser.setDepartments(departments);
        }

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
        // 密码强度校验
        PasswordValidator.validate(
                userUpdatePasswordRequest.password(),
                targetUser.getAuth().getUsername(),
                PasswordValidationRule.DEFAULT
        );

        targetUser.getAuth().setPassword(passwordEncoder.encode(userUpdatePasswordRequest.password()));
        userRepository.save(targetUser);
    }
}
