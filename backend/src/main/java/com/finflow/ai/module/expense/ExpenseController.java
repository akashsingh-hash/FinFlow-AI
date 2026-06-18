package com.finflow.ai.module.expense;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.expense.dto.ExpenseResponse;
import com.finflow.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ExpenseResponse> createDraftExpense(
            @RequestPart("expense") @Valid ExpenseRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        ExpenseResponse response = expenseService.createDraftExpense(request, file, principal.getUsername());
        return ApiResponse.success(response, "Expense draft created successfully");
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<ExpenseResponse> submitExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ExpenseResponse response = expenseService.submitExpense(id, principal.getUsername());
        return ApiResponse.success(response, "Expense claim submitted for approval successfully");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @RequestPart("expense") @Valid ExpenseRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        ExpenseResponse response = expenseService.updateExpense(id, request, file, principal.getUsername());
        return ApiResponse.success(response, "Expense claim updated successfully");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        expenseService.deleteExpense(id, principal.getUsername());
        return ApiResponse.success(null, "Expense draft deleted successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ApiResponse.success(response, "Expense claim retrieved successfully");
    }

    @GetMapping("/my")
    public ApiResponse<List<ExpenseResponse>> getMyExpenses(@AuthenticationPrincipal UserPrincipal principal) {
        List<ExpenseResponse> responses = expenseService.getMyExpenses(principal.getUsername());
        return ApiResponse.success(responses, "Your expense claims retrieved successfully");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'AUDITOR')")
    public ApiResponse<List<ExpenseResponse>> getCompanyExpenses(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<ExpenseResponse> responses = expenseService.getCompanyExpenses(companyId);
        return ApiResponse.success(responses, "Company expense directory retrieved successfully");
    }

    @GetMapping("/review")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE_MANAGER')")
    public ApiResponse<List<ExpenseResponse>> getManagerExpensesToReview(@AuthenticationPrincipal UserPrincipal principal) {
        List<ExpenseResponse> responses = expenseService.getManagerExpensesToReview(principal.getUsername());
        return ApiResponse.success(responses, "Pending reviews retrieved successfully");
    }
}
