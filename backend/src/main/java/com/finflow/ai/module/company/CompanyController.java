package com.finflow.ai.module.company;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.company.dto.CompanyRequest;
import com.finflow.ai.module.company.dto.CompanyResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ApiResponse.success(response, "Company created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<CompanyResponse> updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.updateCompany(id, request);
        return ApiResponse.success(response, "Company updated successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<CompanyResponse> getCompanyById(@PathVariable Long id) {
        CompanyResponse response = companyService.getCompanyById(id);
        return ApiResponse.success(response, "Company retrieved successfully");
    }
}
