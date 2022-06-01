package com.elon.demo.user;

import com.elon.demo.authentication.model.MyUserDetails;
import com.elon.demo.user.model.User;
import com.elon.demo.user.model.UserCreateRequest;
import com.elon.demo.user.model.UserUpdateRequest;
import com.elon.demo.user.model.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Tag(name = "user")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

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

    @GetMapping()
    @Operation(summary = "get user paging information")
    public Page<UserVo> getUserInfo(@RequestParam(required = false) String name,
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

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @PutMapping
    public void save(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        User user = userMapper.toUser(userUpdateRequest);
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
