package com.finflow.ai.module.vendor;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.vendor.dto.VendorRequest;
import com.finflow.ai.module.vendor.dto.VendorResponse;
import com.finflow.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<VendorResponse> addVendor(
            @Valid @RequestBody VendorRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        VendorResponse response = vendorService.addVendor(request, companyId);
        return ApiResponse.success(response, "Vendor added successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'MANAGER')")
    public ApiResponse<VendorResponse> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody VendorRequest request) {
        VendorResponse response = vendorService.updateVendor(id, request);
        return ApiResponse.success(response, "Vendor updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ApiResponse.success(null, "Vendor deleted successfully");
    }

    @GetMapping
    public ApiResponse<List<VendorResponse>> getCompanyVendors(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<VendorResponse> responses = vendorService.getCompanyVendors(companyId);
        return ApiResponse.success(responses, "Vendors retrieved successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<VendorResponse> getVendorById(@PathVariable Long id) {
        VendorResponse response = vendorService.getVendorById(id);
        return ApiResponse.success(response, "Vendor retrieved successfully");
    }
}
