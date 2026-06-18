package com.finflow.ai.module.approval;

import com.finflow.ai.module.approval.dto.ApprovalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ApprovalMapper {
    ApprovalMapper INSTANCE = Mappers.getMapper(ApprovalMapper.class);

    @Mapping(target = "expenseId", source = "expense.id")
    @Mapping(target = "expenseTitle", source = "expense.title")
    @Mapping(target = "expenseAmount", source = "expense.amount")
    @Mapping(target = "expenseUserFullName", expression = "java(approvalWorkflow.getExpense().getUser().getFirstName() + \" \" + approvalWorkflow.getExpense().getUser().getLastName())")
    @Mapping(target = "approverId", source = "approver.id")
    @Mapping(target = "approverFullName", expression = "java(approvalWorkflow.getApprover() != null ? approvalWorkflow.getApprover().getFirstName() + \" \" + approvalWorkflow.getApprover().getLastName() : \"Finance Pool\")")
    ApprovalResponse toResponse(ApprovalWorkflow approvalWorkflow);
}
