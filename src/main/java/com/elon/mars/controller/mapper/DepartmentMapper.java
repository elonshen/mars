package com.elon.mars.controller.mapper;

import com.elon.mars.controller.dto.DepartmentVO;
import com.elon.mars.domain.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "userIds", expression = "java(department.getUsers().stream().map(user -> user.getId()).collect(java.util.stream.Collectors.toSet()))")
    DepartmentVO toDepartmentVO(Department department);

    List<DepartmentVO> toDepartmentVOs(List<Department> departments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Department toDepartment(DepartmentVO departmentVO);
}