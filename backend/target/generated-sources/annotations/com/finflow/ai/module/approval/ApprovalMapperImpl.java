package com.finflow.ai.module.approval;

import com.finflow.ai.module.approval.dto.ApprovalResponse;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.user.User;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:36+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class ApprovalMapperImpl implements ApprovalMapper {

    @Override
    public ApprovalResponse toResponse(ApprovalWorkflow approvalWorkflow) {
        if ( approvalWorkflow == null ) {
            return null;
        }

        ApprovalResponse.ApprovalResponseBuilder approvalResponse = ApprovalResponse.builder();

        approvalResponse.expenseId( approvalWorkflowExpenseId( approvalWorkflow ) );
        approvalResponse.expenseTitle( approvalWorkflowExpenseTitle( approvalWorkflow ) );
        approvalResponse.expenseAmount( approvalWorkflowExpenseAmount( approvalWorkflow ) );
        approvalResponse.approverId( approvalWorkflowApproverId( approvalWorkflow ) );
        approvalResponse.id( approvalWorkflow.getId() );
        approvalResponse.status( approvalWorkflow.getStatus() );
        approvalResponse.comments( approvalWorkflow.getComments() );
        approvalResponse.createdAt( approvalWorkflow.getCreatedAt() );
        approvalResponse.updatedAt( approvalWorkflow.getUpdatedAt() );

        approvalResponse.expenseUserFullName( approvalWorkflow.getExpense().getUser().getFirstName() + " " + approvalWorkflow.getExpense().getUser().getLastName() );
        approvalResponse.approverFullName( approvalWorkflow.getApprover() != null ? approvalWorkflow.getApprover().getFirstName() + " " + approvalWorkflow.getApprover().getLastName() : "Finance Pool" );

        return approvalResponse.build();
    }

    private Long approvalWorkflowExpenseId(ApprovalWorkflow approvalWorkflow) {
        if ( approvalWorkflow == null ) {
            return null;
        }
        Expense expense = approvalWorkflow.getExpense();
        if ( expense == null ) {
            return null;
        }
        Long id = expense.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String approvalWorkflowExpenseTitle(ApprovalWorkflow approvalWorkflow) {
        if ( approvalWorkflow == null ) {
            return null;
        }
        Expense expense = approvalWorkflow.getExpense();
        if ( expense == null ) {
            return null;
        }
        String title = expense.getTitle();
        if ( title == null ) {
            return null;
        }
        return title;
    }

    private BigDecimal approvalWorkflowExpenseAmount(ApprovalWorkflow approvalWorkflow) {
        if ( approvalWorkflow == null ) {
            return null;
        }
        Expense expense = approvalWorkflow.getExpense();
        if ( expense == null ) {
            return null;
        }
        BigDecimal amount = expense.getAmount();
        if ( amount == null ) {
            return null;
        }
        return amount;
    }

    private Long approvalWorkflowApproverId(ApprovalWorkflow approvalWorkflow) {
        if ( approvalWorkflow == null ) {
            return null;
        }
        User approver = approvalWorkflow.getApprover();
        if ( approver == null ) {
            return null;
        }
        Long id = approver.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
