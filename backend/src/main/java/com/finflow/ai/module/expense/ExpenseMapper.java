package com.finflow.ai.module.expense;

import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.expense.dto.ExpenseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    ExpenseMapper INSTANCE = Mappers.getMapper(ExpenseMapper.class);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userFullName", expression = "java(expense.getUser().getFirstName() + \" \" + expense.getUser().getLastName())")
    @Mapping(target = "budgetId", source = "budget.id")
    @Mapping(target = "budgetDepartment", source = "budget.department")
    ExpenseResponse toResponse(Expense expense);

    Expense toEntity(ExpenseRequest request);

    void updateEntityFromRequest(ExpenseRequest request, @MappingTarget Expense expense);
}
