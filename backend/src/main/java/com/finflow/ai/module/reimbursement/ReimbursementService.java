package com.finflow.ai.module.reimbursement;

import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.reimbursement.dto.ReimbursementPaymentRequest;
import com.finflow.ai.module.reimbursement.dto.ReimbursementResponse;

import java.util.List;

public interface ReimbursementService {
    void initiateReimbursement(Expense expense);
    ReimbursementResponse payReimbursement(Long id, ReimbursementPaymentRequest request, String financeManagerEmail);
    ReimbursementResponse getReimbursementById(Long id);
    List<ReimbursementResponse> getCompanyReimbursements(Long companyId);
    List<ReimbursementResponse> getMyReimbursements(String email);
}
