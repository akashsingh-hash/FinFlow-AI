package com.finflow.ai.module.approval;

import com.finflow.ai.module.approval.dto.ApprovalResponse;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.user.User;

import java.util.List;

public interface ApprovalService {
    void createApprovalStep(Expense expense, User approver);
    ApprovalResponse approveExpense(Long approvalId, String comments, String approverEmail);
    ApprovalResponse rejectExpense(Long approvalId, String comments, String approverEmail);
    ApprovalResponse claimApprovalStep(Long approvalId, String approverEmail);
    List<ApprovalResponse> getPendingApprovals(String approverEmail);
    List<ApprovalResponse> getUnassignedPoolApprovals(Long companyId);
    List<ApprovalResponse> getExpenseApprovalHistory(Long expenseId);
}
