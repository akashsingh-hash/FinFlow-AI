package com.finflow.ai.module.expense;

import com.finflow.ai.common.storage.StorageService;
import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.approval.ApprovalRepository;
import com.finflow.ai.module.approval.ApprovalStatus;
import com.finflow.ai.module.approval.ApprovalWorkflow;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.expense.dto.ExpenseResponse;
import com.finflow.ai.module.reimbursement.Reimbursement;
import com.finflow.ai.module.reimbursement.ReimbursementRepository;
import com.finflow.ai.module.reimbursement.ReimbursementStatus;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    @Autowired
    private ExpenseMapper expenseMapper;

    @Autowired
    private StorageService storageService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public ExpenseResponse createDraftExpense(ExpenseRequest request, MultipartFile file, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Enforce active departmental budget lookup
        Budget budget = budgetRepository.findActiveBudget(user.getCompany().getId(), user.getDepartment(), LocalDate.now())
                .orElseThrow(() -> new BusinessException("No active budget found for your department (" 
                        + user.getDepartment() + "). Please contact your administrator."));

        String receiptUrl = null;
        if (file != null && !file.isEmpty()) {
            receiptUrl = storageService.store(file, "receipts");
        }

        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(user);
        expense.setBudget(budget);
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setReceiptUrl(receiptUrl);

        Expense saved = expenseRepository.save(expense);
        auditLogService.log("CREATE_EXPENSE_DRAFT", "Expense", saved.getId(), null, saved);

        return expenseMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseResponse submitExpense(Long id, String email) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new BusinessException("You are not authorized to submit this expense claim.");
        }

        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BusinessException("Expense is already submitted. Current status: " + expense.getStatus().name());
        }

        Budget budget = expense.getBudget();
        if (budget == null) {
            throw new BusinessException("No department budget associated with this claim.");
        }

        BigDecimal requestedAmount = expense.getAmount();

        // 1. Core Rule: Budget Limit Overrun check
        BigDecimal remainingBudget = budget.getAllocatedAmount().subtract(budget.getUtilizedAmount());
        if (remainingBudget.compareTo(requestedAmount) < 0) {
            throw new BusinessException("Insufficient departmental budget. Remaining budget: " 
                    + remainingBudget + ", requested: " + requestedAmount);
        }

        ExpenseStatus oldStatus = expense.getStatus();

        // 2. Approval Routing Logic
        if (requestedAmount.compareTo(new BigDecimal("5000")) < 0) {
            // Rule 1: < 5000 -> Auto Approve
            expense.setStatus(ExpenseStatus.APPROVED);
            
            // Deduct from budget
            budget.setUtilizedAmount(budget.getUtilizedAmount().add(requestedAmount));
            budgetRepository.save(budget);

            // Generate reimbursement record
            Reimbursement reimbursement = Reimbursement.builder()
                    .expense(expense)
                    .status(ReimbursementStatus.PENDING)
                    .build();
            reimbursementRepository.save(reimbursement);

            log.info("Expense ID {} auto-approved. Generated Reimbursement.", expense.getId());
            auditLogService.log("AUTO_APPROVE_EXPENSE", "Expense", expense.getId(), oldStatus.name(), ExpenseStatus.APPROVED.name());

        } else if (requestedAmount.compareTo(new BigDecimal("25000")) <= 0) {
            // Rule 2: 5000 - 25000 -> Manager Approval
            User manager = expense.getUser().getManager();
            if (manager == null) {
                throw new BusinessException("No manager assigned to your profile. A manager is required for approval of expenses between 5000 and 25000.");
            }

            expense.setStatus(ExpenseStatus.UNDER_REVIEW);
            
            ApprovalWorkflow workflow = ApprovalWorkflow.builder()
                    .expense(expense)
                    .approver(manager)
                    .status(ApprovalStatus.PENDING)
                    .build();
            approvalRepository.save(workflow);

            log.info("Expense ID {} routed to Manager {} for approval.", expense.getId(), manager.getEmail());
            auditLogService.log("SUBMIT_EXPENSE_TO_MANAGER", "Expense", expense.getId(), oldStatus.name(), ExpenseStatus.UNDER_REVIEW.name());

        } else {
            // Rule 3: > 25000 -> Finance Manager Approval
            expense.setStatus(ExpenseStatus.UNDER_REVIEW);
            
            ApprovalWorkflow workflow = ApprovalWorkflow.builder()
                    .expense(expense)
                    .approver(null) // Unassigned pool for finance managers
                    .status(ApprovalStatus.PENDING)
                    .build();
            approvalRepository.save(workflow);

            log.info("Expense ID {} routed to Finance Manager Pool for approval.", expense.getId());
            auditLogService.log("SUBMIT_EXPENSE_TO_FINANCE", "Expense", expense.getId(), oldStatus.name(), ExpenseStatus.UNDER_REVIEW.name());
        }

        Expense updated = expenseRepository.save(expense);
        return expenseMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request, MultipartFile file, String email) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new BusinessException("You are not authorized to edit this expense.");
        }

        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BusinessException("Only draft expenses can be modified.");
        }

        Expense oldExpenseCopy = Expense.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .build();

        expenseMapper.updateEntityFromRequest(request, expense);

        if (file != null && !file.isEmpty()) {
            if (expense.getReceiptUrl() != null) {
                storageService.delete(expense.getReceiptUrl());
            }
            String newReceiptUrl = storageService.store(file, "receipts");
            expense.setReceiptUrl(newReceiptUrl);
        }

        Expense updated = expenseRepository.save(expense);
        auditLogService.log("UPDATE_EXPENSE", "Expense", updated.getId(), oldExpenseCopy, updated);

        return expenseMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id, String email) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getEmail().equals(email)) {
            throw new BusinessException("You are not authorized to delete this expense.");
        }

        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new BusinessException("Only draft expenses can be deleted.");
        }

        if (expense.getReceiptUrl() != null) {
            storageService.delete(expense.getReceiptUrl());
        }

        expenseRepository.delete(expense);
        auditLogService.log("DELETE_EXPENSE", "Expense", id, expense, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return expenseMapper.toResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getMyExpenses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return expenseRepository.findByUserId(user.getId()).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getCompanyExpenses(Long companyId) {
        return expenseRepository.findByUserCompanyId(companyId).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getManagerExpensesToReview(String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Manager profile not found"));
        return expenseRepository.findByUserManagerId(manager.getId()).stream()
                .filter(expense -> expense.getStatus() == ExpenseStatus.UNDER_REVIEW)
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }
}
