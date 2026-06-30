package com.finflow.ai.module.reimbursement;

import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.reimbursement.dto.ReimbursementResponse;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:36+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class ReimbursementMapperImpl implements ReimbursementMapper {

    @Override
    public ReimbursementResponse toResponse(Reimbursement reimbursement) {
        if ( reimbursement == null ) {
            return null;
        }

        ReimbursementResponse.ReimbursementResponseBuilder reimbursementResponse = ReimbursementResponse.builder();

        reimbursementResponse.expenseId( reimbursementExpenseId( reimbursement ) );
        reimbursementResponse.expenseTitle( reimbursementExpenseTitle( reimbursement ) );
        reimbursementResponse.expenseAmount( reimbursementExpenseAmount( reimbursement ) );
        reimbursementResponse.expenseCategory( reimbursementExpenseCategory( reimbursement ) );
        reimbursementResponse.id( reimbursement.getId() );
        reimbursementResponse.status( reimbursement.getStatus() );
        reimbursementResponse.paymentMethod( reimbursement.getPaymentMethod() );
        reimbursementResponse.paymentReference( reimbursement.getPaymentReference() );
        reimbursementResponse.paidAt( reimbursement.getPaidAt() );
        reimbursementResponse.createdAt( reimbursement.getCreatedAt() );
        reimbursementResponse.updatedAt( reimbursement.getUpdatedAt() );

        reimbursementResponse.employeeFullName( reimbursement.getExpense().getUser().getFirstName() + " " + reimbursement.getExpense().getUser().getLastName() );

        return reimbursementResponse.build();
    }

    private Long reimbursementExpenseId(Reimbursement reimbursement) {
        if ( reimbursement == null ) {
            return null;
        }
        Expense expense = reimbursement.getExpense();
        if ( expense == null ) {
            return null;
        }
        Long id = expense.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String reimbursementExpenseTitle(Reimbursement reimbursement) {
        if ( reimbursement == null ) {
            return null;
        }
        Expense expense = reimbursement.getExpense();
        if ( expense == null ) {
            return null;
        }
        String title = expense.getTitle();
        if ( title == null ) {
            return null;
        }
        return title;
    }

    private BigDecimal reimbursementExpenseAmount(Reimbursement reimbursement) {
        if ( reimbursement == null ) {
            return null;
        }
        Expense expense = reimbursement.getExpense();
        if ( expense == null ) {
            return null;
        }
        BigDecimal amount = expense.getAmount();
        if ( amount == null ) {
            return null;
        }
        return amount;
    }

    private String reimbursementExpenseCategory(Reimbursement reimbursement) {
        if ( reimbursement == null ) {
            return null;
        }
        Expense expense = reimbursement.getExpense();
        if ( expense == null ) {
            return null;
        }
        String category = expense.getCategory();
        if ( category == null ) {
            return null;
        }
        return category;
    }
}
