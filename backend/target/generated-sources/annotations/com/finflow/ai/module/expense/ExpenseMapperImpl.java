package com.finflow.ai.module.expense;

import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.expense.dto.ExpenseResponse;
import com.finflow.ai.module.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:36+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
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
        expenseResponse.id( expense.getId() );
        expenseResponse.title( expense.getTitle() );
        expenseResponse.amount( expense.getAmount() );
        expenseResponse.description( expense.getDescription() );
        expenseResponse.status( expense.getStatus() );
        expenseResponse.category( expense.getCategory() );
        expenseResponse.receiptUrl( expense.getReceiptUrl() );
        expenseResponse.createdAt( expense.getCreatedAt() );
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

        expense.title( request.getTitle() );
        expense.amount( request.getAmount() );
        expense.description( request.getDescription() );
        expense.category( request.getCategory() );

        return expense.build();
    }

    @Override
    public void updateEntityFromRequest(ExpenseRequest request, Expense expense) {
        if ( request == null ) {
            return;
        }

        expense.setTitle( request.getTitle() );
        expense.setAmount( request.getAmount() );
        expense.setDescription( request.getDescription() );
        expense.setCategory( request.getCategory() );
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
