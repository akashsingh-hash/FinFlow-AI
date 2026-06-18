package com.finflow.ai.module.expense;

import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.expense.dto.ExpenseResponse;
import com.finflow.ai.module.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T13:35:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ExpenseMapperImpl implements ExpenseMapper {

    @Override
    public ExpenseResponse toResponse(Expense expense) {
        if ( expense == null ) {
            return null;
        }

        ExpenseResponse.ExpenseResponseBuilder expenseResponse = ExpenseResponse.builder();

        expenseResponse.userId( expenseUserId( expense ) );
        expenseResponse.userEmail( expenseUserEmail( expense ) );
        expenseResponse.budgetId( expenseBudgetId( expense ) );
        expenseResponse.budgetDepartment( expenseBudgetDepartment( expense ) );
        expenseResponse.amount( expense.getAmount() );
        expenseResponse.category( expense.getCategory() );
        expenseResponse.createdAt( expense.getCreatedAt() );
        expenseResponse.description( expense.getDescription() );
        expenseResponse.id( expense.getId() );
        expenseResponse.receiptUrl( expense.getReceiptUrl() );
        expenseResponse.status( expense.getStatus() );
        expenseResponse.title( expense.getTitle() );
        expenseResponse.updatedAt( expense.getUpdatedAt() );

        expenseResponse.userFullName( expense.getUser().getFirstName() + " " + expense.getUser().getLastName() );

        return expenseResponse.build();
    }

    @Override
    public Expense toEntity(ExpenseRequest request) {
        if ( request == null ) {
            return null;
        }

        Expense.ExpenseBuilder expense = Expense.builder();

        expense.amount( request.getAmount() );
        expense.category( request.getCategory() );
        expense.description( request.getDescription() );
        expense.title( request.getTitle() );

        return expense.build();
    }

    @Override
    public void updateEntityFromRequest(ExpenseRequest request, Expense expense) {
        if ( request == null ) {
            return;
        }

        expense.setAmount( request.getAmount() );
        expense.setCategory( request.getCategory() );
        expense.setDescription( request.getDescription() );
        expense.setTitle( request.getTitle() );
    }

    private Long expenseUserId(Expense expense) {
        if ( expense == null ) {
            return null;
        }
        User user = expense.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String expenseUserEmail(Expense expense) {
        if ( expense == null ) {
            return null;
        }
        User user = expense.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    private Long expenseBudgetId(Expense expense) {
        if ( expense == null ) {
            return null;
        }
        Budget budget = expense.getBudget();
        if ( budget == null ) {
            return null;
        }
        Long id = budget.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String expenseBudgetDepartment(Expense expense) {
        if ( expense == null ) {
            return null;
        }
        Budget budget = expense.getBudget();
        if ( budget == null ) {
            return null;
        }
        String department = budget.getDepartment();
        if ( department == null ) {
            return null;
        }
        return department;
    }
}
