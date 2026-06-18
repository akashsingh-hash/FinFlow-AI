package com.finflow.ai.module.budget;

import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.budget.dto.BudgetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    BudgetMapper INSTANCE = Mappers.getMapper(BudgetMapper.class);

    @Mapping(target = "companyId", source = "company.id")
    BudgetResponse toResponse(Budget budget);

    Budget toEntity(BudgetRequest request);

    void updateEntityFromRequest(BudgetRequest request, @MappingTarget Budget budget);
}
