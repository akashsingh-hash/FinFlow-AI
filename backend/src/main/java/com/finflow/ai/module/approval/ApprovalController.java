package com.finflow.ai.module.approval;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.approval.dto.ApprovalRequest;
import com.finflow.ai.module.approval.dto.ApprovalResponse;
import com.finflow.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approvals")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'MANAGER')")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @PatchMapping("/{id}/approve")
    public ApiResponse<ApprovalResponse> approveExpense(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String comments = request != null ? request.getComments() : "Approved";
        ApprovalResponse response = approvalService.approveExpense(id, comments, principal.getUsername());
        return ApiResponse.success(response, "Expense claim approved");
    }

    @PatchMapping("/{id}/reject")
    public ApiResponse<ApprovalResponse> rejectExpense(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String comments = request.getComments() != null ? request.getComments() : "Rejected";
        ApprovalResponse response = approvalService.rejectExpense(id, comments, principal.getUsername());
        return ApiResponse.success(response, "Expense claim rejected");
    }

    @PatchMapping("/{id}/claim")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    public ApiResponse<ApprovalResponse> claimApprovalStep(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ApprovalResponse response = approvalService.claimApprovalStep(id, principal.getUsername());
        return ApiResponse.success(response, "Approval step claimed successfully");
    }

    @GetMapping("/pending")
    public ApiResponse<List<ApprovalResponse>> getPendingApprovals(@AuthenticationPrincipal UserPrincipal principal) {
        List<ApprovalResponse> responses = approvalService.getPendingApprovals(principal.getUsername());
        return ApiResponse.success(responses, "Pending approvals retrieved successfully");
    }

    @GetMapping("/history/{expenseId}")
    public ApiResponse<List<ApprovalResponse>> getExpenseApprovalHistory(@PathVariable Long expenseId) {
        List<ApprovalResponse> responses = approvalService.getExpenseApprovalHistory(expenseId);
        return ApiResponse.success(responses, "Expense approval history retrieved successfully");
    }
}
