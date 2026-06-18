package com.finflow.ai.module.reimbursement;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.expense.ExpenseRepository;
import com.finflow.ai.module.expense.ExpenseStatus;
import com.finflow.ai.module.reimbursement.dto.ReimbursementPaymentRequest;
import com.finflow.ai.module.reimbursement.dto.ReimbursementResponse;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReimbursementMapper reimbursementMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public void initiateReimbursement(Expense expense) {
        Reimbursement reimbursement = Reimbursement.builder()
                .expense(expense)
                .status(ReimbursementStatus.PENDING)
                .build();
        reimbursementRepository.save(reimbursement);
    }

    @Override
    @Transactional
    public ReimbursementResponse payReimbursement(Long id, ReimbursementPaymentRequest request, String financeManagerEmail) {
        Reimbursement reimbursement = reimbursementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reimbursement payout not found"));

        if (reimbursement.getStatus() == ReimbursementStatus.PAID) {
            throw new BusinessException("This reimbursement claim has already been paid.");
        }

        User payer = userRepository.findByEmail(financeManagerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (payer.getRole() != Role.FINANCE_MANAGER && payer.getRole() != Role.ADMIN) {
            throw new BusinessException("Only users with FINANCE_MANAGER or ADMIN role can record payouts.");
        }

        ReimbursementStatus oldStatus = reimbursement.getStatus();

        // Perform payment transitions
        reimbursement.setStatus(ReimbursementStatus.PAID);
        reimbursement.setPaymentMethod(request.getPaymentMethod());
        reimbursement.setPaymentReference(request.getPaymentReference());
        reimbursement.setPaidAt(LocalDateTime.now());
        Reimbursement saved = reimbursementRepository.save(reimbursement);

        // Update target expense state to REIMBURSED
        Expense expense = reimbursement.getExpense();
        ExpenseStatus oldExpenseStatus = expense.getStatus();
        expense.setStatus(ExpenseStatus.REIMBURSED);
        expenseRepository.save(expense);

        log.info("Reimbursement ID {} paid. Expense ID {} set to REIMBURSED.", id, expense.getId());
        auditLogService.log("PAY_REIMBURSEMENT", "Reimbursement", id, oldStatus.name(), ReimbursementStatus.PAID.name());
        auditLogService.log("REIMBURSE_EXPENSE", "Expense", expense.getId(), oldExpenseStatus.name(), ExpenseStatus.REIMBURSED.name());

        return reimbursementMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReimbursementResponse getReimbursementById(Long id) {
        Reimbursement reimbursement = reimbursementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reimbursement not found"));
        return reimbursementMapper.toResponse(reimbursement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReimbursementResponse> getCompanyReimbursements(Long companyId) {
        return reimbursementRepository.findByExpenseUserCompanyId(companyId).stream()
                .map(reimbursementMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReimbursementResponse> getMyReimbursements(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return reimbursementRepository.findByExpenseUserId(user.getId()).stream()
                .map(reimbursementMapper::toResponse)
                .collect(Collectors.toList());
    }
}
