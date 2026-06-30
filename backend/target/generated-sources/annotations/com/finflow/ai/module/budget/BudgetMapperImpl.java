package com.finflow.ai.module.budget;

import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.budget.dto.BudgetResponse;
import com.finflow.ai.module.company.Company;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:36+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class BudgetMapperImpl implements BudgetMapper {

    @Override
    public BudgetResponse toResponse(Budget budget) {
        if ( budget == null ) {
            return null;
        }

        BudgetResponse.BudgetResponseBuilder budgetResponse = BudgetResponse.builder();

        budgetResponse.companyId( budgetCompanyId( budget ) );
        budgetResponse.id( budget.getId() );
        budgetResponse.department( budget.getDepartment() );
        budgetResponse.allocatedAmount( budget.getAllocatedAmount() );
        budgetResponse.utilizedAmount( budget.getUtilizedAmount() );
        budgetResponse.startDate( budget.getStartDate() );
        budgetResponse.endDate( budget.getEndDate() );
        budgetResponse.createdAt( budget.getCreatedAt() );
        budgetResponse.updatedAt( budget.getUpdatedAt() );

        return budgetResponse.build();
    }

    @Override
    public Budget toEntity(BudgetRequest request) {
        if ( request == null ) {
            return null;
        }

        Budget.BudgetBuilder budget = Budget.builder();

        budget.department( request.getDepartment() );
        budget.allocatedAmount( request.getAllocatedAmount() );
        budget.startDate( request.getStartDate() );
        budget.endDate( request.getEndDate() );

        return budget.build();
    }

    @Override
    public void updateEntityFromRequest(BudgetRequest request, Budget budget) {
        if ( request == null ) {
            return;
        }

        budget.setDepartment( request.getDepartment() );
        budget.setAllocatedAmount( request.getAllocatedAmount() );
        budget.setStartDate( request.getStartDate() );
        budget.setEndDate( request.getEndDate() );
    }

    private Long budgetCompanyId(Budget budget) {
        if ( budget == null ) {
            return null;
        }
        Company company = budget.getCompany();
        if ( company == null ) {
            return null;
        }
        Long id = company.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
