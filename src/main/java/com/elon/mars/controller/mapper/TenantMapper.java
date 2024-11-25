package com.elon.mars.controller.mapper;

import com.elon.mars.controller.dto.TenantVO;
import com.elon.mars.domain.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TenantMapper {
    TenantVO toTenantVO(Tenant tenant);

    List<TenantVO> toTenantVOs(List<Tenant> tenants);
}