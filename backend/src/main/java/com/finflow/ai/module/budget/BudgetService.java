package com.finflow.ai.module.budget;

import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.budget.dto.BudgetResponse;

import java.util.List;

public interface BudgetService {
    BudgetResponse createBudget(BudgetRequest request, Long companyId);
    BudgetResponse updateBudget(Long id, BudgetRequest request);
    List<BudgetResponse> getCompanyBudgets(Long companyId);
    BudgetResponse getBudgetById(Long id);
    BudgetResponse getActiveBudgetForDepartment(Long companyId, String department);
}
