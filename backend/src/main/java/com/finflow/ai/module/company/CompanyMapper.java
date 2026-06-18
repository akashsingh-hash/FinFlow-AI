package com.finflow.ai.module.company;

import com.finflow.ai.module.company.dto.CompanyRequest;
import com.finflow.ai.module.company.dto.CompanyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    CompanyResponse toResponse(Company company);
    Company toEntity(CompanyRequest request);
    void updateEntityFromRequest(CompanyRequest request, @MappingTarget Company company);
}
