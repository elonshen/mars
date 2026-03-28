package com.elon.mars.controller.mapper;

import com.elon.mars.controller.dto.RoleCreateRequest;
import com.elon.mars.controller.dto.RoleUpdateRequest;
import com.elon.mars.controller.dto.RoleVO;
import com.elon.mars.domain.Role;
import org.mapstruct.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreateRequest request);

    @Mapping(target = "permissionIds", expression = "java(role.getPermissions().stream().map(permission -> permission.getId()).collect(java.util.stream.Collectors.toSet()))")
    RoleVO toRoleVO(Role role);

    List<RoleVO> toRoleVOs(List<Role> roles);

    default PageImpl<RoleVO> toRoleVOPage(List<Role> content, Pageable pageable, long total) {
        return new PageImpl<>(toRoleVOs(content), pageable, total);
    }

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "permissions", ignore = true)
    void updateRole(RoleUpdateRequest request, @MappingTarget Role role);
}

