package com.finflow.ai.module.analytics;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'MANAGER', 'AUDITOR')")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/monthly-spend")
    public ApiResponse<List<MonthlySpend>> getMonthlySpend(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<MonthlySpend> report = analyticsService.getMonthlySpendReport(companyId);
        return ApiResponse.success(report, "Monthly spending report retrieved successfully");
    }

    @GetMapping("/department-spend")
    public ApiResponse<List<DepartmentSpend>> getDepartmentSpend(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<DepartmentSpend> report = analyticsService.getDepartmentSpendReport(companyId);
        return ApiResponse.success(report, "Departmental spending report retrieved successfully");
    }

    @GetMapping("/vendor-spend")
    public ApiResponse<List<VendorSpend>> getVendorSpend(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<VendorSpend> report = analyticsService.getVendorSpendReport(companyId);
        return ApiResponse.success(report, "Vendor spending report retrieved successfully");
    }

    @GetMapping("/budget-consumption")
    public ApiResponse<List<BudgetConsumption>> getBudgetConsumption(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<BudgetConsumption> report = analyticsService.getBudgetConsumptionReport(companyId);
        return ApiResponse.success(report, "Budget consumption report retrieved successfully");
    }
}
