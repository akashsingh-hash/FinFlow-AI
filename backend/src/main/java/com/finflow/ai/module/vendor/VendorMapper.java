package com.finflow.ai.module.vendor;

import com.finflow.ai.module.vendor.dto.VendorRequest;
import com.finflow.ai.module.vendor.dto.VendorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VendorMapper {
    VendorMapper INSTANCE = Mappers.getMapper(VendorMapper.class);

    @Mapping(target = "companyId", source = "company.id")
    VendorResponse toResponse(Vendor vendor);

    Vendor toEntity(VendorRequest request);

    void updateEntityFromRequest(VendorRequest request, @MappingTarget Vendor vendor);
}
