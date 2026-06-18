package com.finflow.ai.module.approval;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.approval.dto.ApprovalResponse;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.expense.ExpenseRepository;
import com.finflow.ai.module.expense.ExpenseStatus;
import com.finflow.ai.module.reimbursement.Reimbursement;
import com.finflow.ai.module.reimbursement.ReimbursementRepository;
import com.finflow.ai.module.reimbursement.ReimbursementStatus;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    @Autowired
    private ApprovalMapper approvalMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public void createApprovalStep(Expense expense, User approver) {
        ApprovalWorkflow workflow = ApprovalWorkflow.builder()
                .expense(expense)
                .approver(approver)
                .status(ApprovalStatus.PENDING)
                .build();
        approvalRepository.save(workflow);
    }

    @Override
    @Transactional
    public ApprovalResponse approveExpense(Long approvalId, String comments, String approverEmail) {
        ApprovalWorkflow step = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow step not found"));

        if (step.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("This approval step has already been completed.");
        }

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        // Validate authority
        validateApprovalAuthority(step, approver);

        // Update step status
        step.setStatus(ApprovalStatus.APPROVED);
        step.setComments(comments);
        if (step.getApprover() == null) {
            step.setApprover(approver); // Claim step if unassigned
        }
        ApprovalWorkflow savedStep = approvalRepository.save(step);

        // Finalize Expense state
        Expense expense = step.getExpense();
        BigDecimal amount = expense.getAmount();
        Budget budget = expense.getBudget();

        if (budget == null) {
            throw new BusinessException("No department budget linked to this expense.");
        }

        // Recheck budget allocation
        BigDecimal remainingBudget = budget.getAllocatedAmount().subtract(budget.getUtilizedAmount());
        if (remainingBudget.compareTo(amount) < 0) {
            throw new BusinessException("Cannot approve. Department budget overrun. Remaining budget: " 
                    + remainingBudget + ", expense amount: " + amount);
        }

        // Deduct from budget
        budget.setUtilizedAmount(budget.getUtilizedAmount().add(amount));
        budgetRepository.save(budget);

        // Update Expense status to APPROVED
        expense.setStatus(ExpenseStatus.APPROVED);
        expenseRepository.save(expense);

        // Generate Reimbursement entry
        Reimbursement reimbursement = Reimbursement.builder()
                .expense(expense)
                .status(ReimbursementStatus.PENDING)
                .build();
        reimbursementRepository.save(reimbursement);

        log.info("Approval ID {} approved by {}. Expense status set to APPROVED.", approvalId, approverEmail);
        auditLogService.log("APPROVE_EXPENSE_WORKFLOW", "ApprovalWorkflow", approvalId, "PENDING", "APPROVED");

        return approvalMapper.toResponse(savedStep);
    }

    @Override
    @Transactional
    public ApprovalResponse rejectExpense(Long approvalId, String comments, String approverEmail) {
        ApprovalWorkflow step = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow step not found"));

        if (step.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("This approval step has already been completed.");
        }

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        // Validate authority
        validateApprovalAuthority(step, approver);

        // Update step
        step.setStatus(ApprovalStatus.REJECTED);
        step.setComments(comments);
        if (step.getApprover() == null) {
            step.setApprover(approver);
        }
        ApprovalWorkflow savedStep = approvalRepository.save(step);

        // Reject Expense status
        Expense expense = step.getExpense();
        expense.setStatus(ExpenseStatus.REJECTED);
        expenseRepository.save(expense);

        log.info("Approval ID {} rejected by {}. Expense status set to REJECTED.", approvalId, approverEmail);
        auditLogService.log("REJECT_EXPENSE_WORKFLOW", "ApprovalWorkflow", approvalId, "PENDING", "REJECTED");

        return approvalMapper.toResponse(savedStep);
    }

    @Override
    @Transactional
    public ApprovalResponse claimApprovalStep(Long approvalId, String approverEmail) {
        ApprovalWorkflow step = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow step not found"));

        if (step.getApprover() != null) {
            throw new BusinessException("This approval step has already been claimed by " 
                    + step.getApprover().getFirstName() + " " + step.getApprover().getLastName());
        }

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        if (approver.getRole() != Role.FINANCE_MANAGER) {
            throw new BusinessException("Only users with role FINANCE_MANAGER can claim unassigned pool reviews.");
        }

        step.setApprover(approver);
        ApprovalWorkflow savedStep = approvalRepository.save(step);

        auditLogService.log("CLAIM_APPROVAL_STEP", "ApprovalWorkflow", approvalId, "POOL", approverEmail);

        return approvalMapper.toResponse(savedStep);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getPendingApprovals(String approverEmail) {
        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        List<ApprovalWorkflow> list = new ArrayList<>(approvalRepository.findByApproverIdAndStatus(approver.getId(), ApprovalStatus.PENDING));

        // Finance Managers also see unclaimed finance pool approvals of their own company
        if (approver.getRole() == Role.FINANCE_MANAGER) {
            List<ApprovalWorkflow> pool = approvalRepository.findUnassignedPoolApprovals(
                    approver.getCompany().getId(), ApprovalStatus.PENDING
            );
            list.addAll(pool);
        }

        return list.stream()
                .distinct()
                .map(approvalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getUnassignedPoolApprovals(Long companyId) {
        return approvalRepository.findUnassignedPoolApprovals(companyId, ApprovalStatus.PENDING).stream()
                .map(approvalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getExpenseApprovalHistory(Long expenseId) {
        return approvalRepository.findByExpenseId(expenseId).stream()
                .map(approvalMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateApprovalAuthority(ApprovalWorkflow step, User approver) {
        if (step.getApprover() != null) {
            // Direct assignment validation
            if (!step.getApprover().getId().equals(approver.getId())) {
                throw new BusinessException("You are not the assigned approver for this expense claim.");
            }
        } else {
            // Pool assignment validation: Must be FINANCE_MANAGER in same company
            if (approver.getRole() != Role.FINANCE_MANAGER) {
                throw new BusinessException("Only users with FINANCE_MANAGER role can approve this high-value expense claim.");
            }
            if (!step.getExpense().getUser().getCompany().getId().equals(approver.getCompany().getId())) {
                throw new BusinessException("You cannot approve claims from other companies.");
            }
        }
    }
}
