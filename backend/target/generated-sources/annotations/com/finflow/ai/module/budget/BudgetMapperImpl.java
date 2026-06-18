package com.finflow.ai.module.budget;

import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.budget.dto.BudgetResponse;
import com.finflow.ai.module.company.Company;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T13:35:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
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
        budgetResponse.allocatedAmount( budget.getAllocatedAmount() );
        budgetResponse.createdAt( budget.getCreatedAt() );
        budgetResponse.department( budget.getDepartment() );
        budgetResponse.endDate( budget.getEndDate() );
        budgetResponse.id( budget.getId() );
        budgetResponse.startDate( budget.getStartDate() );
        budgetResponse.updatedAt( budget.getUpdatedAt() );
        budgetResponse.utilizedAmount( budget.getUtilizedAmount() );

        return budgetResponse.build();
    }

    @Override
    public Budget toEntity(BudgetRequest request) {
        if ( request == null ) {
            return null;
        }

        Budget.BudgetBuilder budget = Budget.builder();

        budget.allocatedAmount( request.getAllocatedAmount() );
        budget.department( request.getDepartment() );
        budget.endDate( request.getEndDate() );
        budget.startDate( request.getStartDate() );

        return budget.build();
    }

    @Override
    public void updateEntityFromRequest(BudgetRequest request, Budget budget) {
        if ( request == null ) {
            return;
        }

        budget.setAllocatedAmount( request.getAllocatedAmount() );
        budget.setDepartment( request.getDepartment() );
        budget.setEndDate( request.getEndDate() );
        budget.setStartDate( request.getStartDate() );
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
