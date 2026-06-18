package com.finflow.ai.module.budget;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.budget.dto.BudgetResponse;
import com.finflow.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        BudgetResponse response = budgetService.createBudget(request, companyId);
        return ApiResponse.success(response, "Budget created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(id, request);
        return ApiResponse.success(response, "Budget updated successfully");
    }

    @GetMapping
    public ApiResponse<List<BudgetResponse>> getCompanyBudgets(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<BudgetResponse> responses = budgetService.getCompanyBudgets(companyId);
        return ApiResponse.success(responses, "Budgets retrieved successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<BudgetResponse> getBudgetById(@PathVariable Long id) {
        BudgetResponse response = budgetService.getBudgetById(id);
        return ApiResponse.success(response, "Budget retrieved successfully");
    }

    @GetMapping("/active")
    public ApiResponse<BudgetResponse> getActiveBudgetForDepartment(
            @RequestParam String department,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        BudgetResponse response = budgetService.getActiveBudgetForDepartment(companyId, department);
        return ApiResponse.success(response, "Active department budget retrieved successfully");
    }
}
