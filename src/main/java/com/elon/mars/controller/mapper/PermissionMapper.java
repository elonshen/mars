package com.elon.mars.controller.mapper;

import com.elon.mars.controller.dto.PermissionCreateRequest;
import com.elon.mars.controller.dto.PermissionVO;
import com.elon.mars.domain.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionMapper {
    PermissionVO toPermissionVO(Permission permission);

    List<PermissionVO> toPermissionVOs(List<Permission> permissions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Permission toPermission(PermissionCreateRequest request);
}
