package com.finflow.ai.module.approval;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.approval.dto.ApprovalResponse;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.expense.ExpenseRepository;
import com.finflow.ai.module.expense.ExpenseStatus;
import com.finflow.ai.module.reimbursement.Reimbursement;
import com.finflow.ai.module.reimbursement.ReimbursementRepository;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ReimbursementRepository reimbursementRepository;

    @Mock
    private ApprovalMapper approvalMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private Company company;
    private User employee;
    private User manager;
    private User financeManager;
    private Budget budget;
    private Expense expense;
    private ApprovalWorkflow workflowStep;
    private ApprovalResponse approvalResponse;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(1L).name("Acme Corp").build();

        manager = User.builder()
                .id(2L)
                .email("manager@acme.com")
                .role(Role.MANAGER)
                .company(company)
                .build();

        financeManager = User.builder()
                .id(3L)
                .email("finance@acme.com")
                .role(Role.FINANCE_MANAGER)
                .company(company)
                .build();

        employee = User.builder()
                .id(4L)
                .email("employee@acme.com")
                .role(Role.EMPLOYEE)
                .company(company)
                .manager(manager)
                .build();

        budget = Budget.builder()
                .id(10L)
                .company(company)
                .department("Engineering")
                .allocatedAmount(new BigDecimal("50000.00"))
                .utilizedAmount(new BigDecimal("10000.00"))
                .build();

        expense = Expense.builder()
                .id(100L)
                .amount(new BigDecimal("12000.00"))
                .user(employee)
                .budget(budget)
                .status(ExpenseStatus.UNDER_REVIEW)
                .build();

        workflowStep = ApprovalWorkflow.builder()
                .id(500L)
                .expense(expense)
                .approver(manager)
                .status(ApprovalStatus.PENDING)
                .build();

        approvalResponse = ApprovalResponse.builder()
                .id(500L)
                .status(ApprovalStatus.APPROVED)
                .expenseId(100L)
                .approverFullName("Bob Manager")
                .build();
    }

    @Test
    @DisplayName("Should approve manager review successfully, update budget and expense status to APPROVED")
    void testApproveExpenseManagerSuccess() {
        when(approvalRepository.findById(500L)).thenReturn(Optional.of(workflowStep));
        when(userRepository.findByEmail("manager@acme.com")).thenReturn(Optional.of(manager));
        when(approvalRepository.save(any(ApprovalWorkflow.class))).thenReturn(workflowStep);
        when(approvalMapper.toResponse(workflowStep)).thenReturn(approvalResponse);

        ApprovalResponse result = approvalService.approveExpense(500L, "Looks good", "manager@acme.com");

        assertThat(result).isNotNull();
        assertThat(workflowStep.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        assertThat(budget.getUtilizedAmount()).isEqualTo(new BigDecimal("22000.00")); // 10000 + 12000
        verify(reimbursementRepository).save(any(Reimbursement.class));
        verify(budgetRepository).save(budget);
    }

    @Test
    @DisplayName("Should reject approval and set expense status to REJECTED")
    void testRejectExpenseSuccess() {
        when(approvalRepository.findById(500L)).thenReturn(Optional.of(workflowStep));
        when(userRepository.findByEmail("manager@acme.com")).thenReturn(Optional.of(manager));
        when(approvalRepository.save(any(ApprovalWorkflow.class))).thenReturn(workflowStep);
        when(approvalMapper.toResponse(workflowStep)).thenReturn(
                ApprovalResponse.builder().id(500L).status(ApprovalStatus.REJECTED).build()
        );

        ApprovalResponse result = approvalService.rejectExpense(500L, "Receipt is unclear", "manager@acme.com");

        assertThat(workflowStep.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
        verify(expenseRepository).save(expense);
        verifyNoInteractions(reimbursementRepository);
    }

    @Test
    @DisplayName("Should throw BusinessException when unauthorized user attempts to approve")
    void testApproveUnauthorizedUser() {
        User otherManager = User.builder().id(99L).email("other@acme.com").role(Role.MANAGER).company(company).build();
        when(approvalRepository.findById(500L)).thenReturn(Optional.of(workflowStep));
        when(userRepository.findByEmail("other@acme.com")).thenReturn(Optional.of(otherManager));

        assertThatThrownBy(() -> approvalService.approveExpense(500L, "ok", "other@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("You are not the assigned approver");
    }

    @Test
    @DisplayName("Finance manager should successfully approve unassigned pool claim")
    void testFinanceManagerApprovePoolClaim() {
        workflowStep.setApprover(null); // pool claim

        when(approvalRepository.findById(500L)).thenReturn(Optional.of(workflowStep));
        when(userRepository.findByEmail("finance@acme.com")).thenReturn(Optional.of(financeManager));
        when(approvalRepository.save(any(ApprovalWorkflow.class))).thenReturn(workflowStep);
        when(approvalMapper.toResponse(workflowStep)).thenReturn(approvalResponse);

        ApprovalResponse result = approvalService.approveExpense(500L, "Approved pool claim", "finance@acme.com");

        assertThat(workflowStep.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(workflowStep.getApprover()).isEqualTo(financeManager);
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
    }
}
