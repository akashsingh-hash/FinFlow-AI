package com.finflow.ai.module.reimbursement;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.reimbursement.dto.ReimbursementPaymentRequest;
import com.finflow.ai.module.reimbursement.dto.ReimbursementResponse;
import com.finflow.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reimbursements")
public class ReimbursementController {

    @Autowired
    private ReimbursementService reimbursementService;

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<ReimbursementResponse> payReimbursement(
            @PathVariable Long id,
            @Valid @RequestBody ReimbursementPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ReimbursementResponse response = reimbursementService.payReimbursement(id, request, principal.getUsername());
        return ApiResponse.success(response, "Reimbursement paid successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<ReimbursementResponse> getReimbursementById(@PathVariable Long id) {
        ReimbursementResponse response = reimbursementService.getReimbursementById(id);
        return ApiResponse.success(response, "Reimbursement retrieved successfully");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'AUDITOR')")
    public ApiResponse<List<ReimbursementResponse>> getCompanyReimbursements(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<ReimbursementResponse> responses = reimbursementService.getCompanyReimbursements(companyId);
        return ApiResponse.success(responses, "Company disbursements retrieved successfully");
    }

    @GetMapping("/my")
    public ApiResponse<List<ReimbursementResponse>> getMyReimbursements(@AuthenticationPrincipal UserPrincipal principal) {
        List<ReimbursementResponse> responses = reimbursementService.getMyReimbursements(principal.getUsername());
        return ApiResponse.success(responses, "Your disbursements retrieved successfully");
    }
}
