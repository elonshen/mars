package com.elon.mars.controller.mapper;

import com.elon.mars.controller.dto.UserCreateRequest;
import com.elon.mars.controller.dto.UserUpdateRequest;
import com.elon.mars.controller.dto.UserVO;
import com.elon.mars.domain.Department;
import com.elon.mars.domain.Role;
import com.elon.mars.domain.Permission;
import com.elon.mars.domain.User;
import org.mapstruct.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",imports = {Role.class, Department.class,Collectors.class})
public interface UserMapper {
    @Mapping(target = "auth", expression = "java(new Auth())")
    @Mapping(target = "auth.username", source = "username")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "departments", ignore = true)
    User toUser(UserCreateRequest userCreateRequest);

    @Mapping(target = "username", source = "auth.username")
    @Mapping(target = "roleIds", source = "roles")
    @Mapping(target = "departmentIds", source = "departments")
    @Mapping(target = "roleNames",expression = "java(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))")
    @Mapping(target = "departmentNames",expression = "java(user.getDepartments().stream().map(Department::getName).collect(Collectors.toSet()))")
    @Mapping(target = "permissionCodes",source = "roles")
    UserVO toUserVo(User user);

    List<UserVO> toUserVos(List<User> users);

    default PageImpl<UserVO> toUserVoPage(List<User> content, Pageable pageable, long total) {
        return new PageImpl<>(toUserVos(content), pageable, total);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "auth.username", source = "username")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "departments", ignore = true)
    void updatePerson(UserUpdateRequest userUpdateRequest, @MappingTarget User user);

    default Set<Long> rolesToIds(Set<Role> roles) {
        if (roles == null) {
            return new HashSet<>();
        }
        return roles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
    }

    default Set<Long> departmentsToIds(Set<Department> departments) {
        if (departments == null) {
            return new HashSet<>();
        }
        return departments.stream()
                .map(Department::getId)
                .collect(Collectors.toSet());
    }
    default Set<String> rolesToPermissionCodes(Set<Role> roles) {
        if (roles == null) {
            return new HashSet<>();
        }
        return roles.stream()
                .map(Role::getPermissions)
                .flatMap(Set::stream)
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }
}