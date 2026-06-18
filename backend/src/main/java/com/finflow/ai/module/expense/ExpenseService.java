package com.finflow.ai.module.expense;

import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.expense.dto.ExpenseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse createDraftExpense(ExpenseRequest request, MultipartFile file, String email);
    ExpenseResponse submitExpense(Long id, String email);
    ExpenseResponse updateExpense(Long id, ExpenseRequest request, MultipartFile file, String email);
    void deleteExpense(Long id, String email);
    ExpenseResponse getExpenseById(Long id);
    List<ExpenseResponse> getMyExpenses(String email);
    List<ExpenseResponse> getCompanyExpenses(Long companyId);
    List<ExpenseResponse> getManagerExpensesToReview(String managerEmail);
}
