package com.finflow.ai.module.reimbursement;

import com.finflow.ai.module.reimbursement.dto.ReimbursementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReimbursementMapper {
    ReimbursementMapper INSTANCE = Mappers.getMapper(ReimbursementMapper.class);

    @Mapping(target = "expenseId", source = "expense.id")
    @Mapping(target = "expenseTitle", source = "expense.title")
    @Mapping(target = "expenseAmount", source = "expense.amount")
    @Mapping(target = "expenseCategory", source = "expense.category")
    @Mapping(target = "employeeFullName", expression = "java(reimbursement.getExpense().getUser().getFirstName() + \" \" + reimbursement.getExpense().getUser().getLastName())")
    ReimbursementResponse toResponse(Reimbursement reimbursement);
}
