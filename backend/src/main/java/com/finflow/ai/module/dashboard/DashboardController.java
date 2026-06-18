package com.finflow.ai.module.dashboard;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStats> getDashboardStats(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        String email = principal.getUsername();
        String roleName = "ROLE_" + principal.getUser().getRole().name();
        
        DashboardStats stats = dashboardService.getCompanyStats(companyId, email, roleName);
        return ApiResponse.success(stats, "Dashboard statistics retrieved successfully");
    }
}
