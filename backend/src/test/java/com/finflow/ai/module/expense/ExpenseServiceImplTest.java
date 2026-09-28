package com.finflow.ai.module.expense;

import com.finflow.ai.common.storage.StorageService;
import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.approval.ApprovalRepository;
import com.finflow.ai.module.approval.ApprovalWorkflow;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.expense.dto.ExpenseResponse;
import com.finflow.ai.module.reimbursement.Reimbursement;
import com.finflow.ai.module.reimbursement.ReimbursementRepository;
import com.finflow.ai.module.reimbursement.ReimbursementStatus;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserRepository;
import com.finflow.ai.module.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private ReimbursementRepository reimbursementRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Company company;
    private User employee;
    private User manager;
    private Budget budget;
    private Expense expense;
    private ExpenseResponse expenseResponse;
    private ExpenseRequest expenseRequest;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(1L).name("Acme Corp").build();

        manager = User.builder()
                .id(2L)
                .email("manager@acme.com")
                .firstName("Bob")
                .lastName("Manager")
                .role(Role.MANAGER)
                .status(UserStatus.ACTIVE)
                .company(company)
                .build();

        employee = User.builder()
                .id(3L)
                .email("employee@acme.com")
                .firstName("Alice")
                .lastName("Employee")
                .role(Role.EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .department("Engineering")
                .manager(manager)
                .company(company)
                .build();

        budget = Budget.builder()
                .id(100L)
                .company(company)
                .department("Engineering")
                .allocatedAmount(new BigDecimal("100000.00"))
                .utilizedAmount(new BigDecimal("20000.00"))
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(5))
                .build();

        expenseRequest = ExpenseRequest.builder()
                .title("Client Dinner")
                .amount(new BigDecimal("3000.00"))
                .category("Meals")
                .description("Meeting with client")
                .build();

        expense = Expense.builder()
                .id(50L)
                .title("Client Dinner")
                .amount(new BigDecimal("3000.00"))
                .category("Meals")
                .user(employee)
                .budget(budget)
                .status(ExpenseStatus.DRAFT)
                .build();

        expenseResponse = ExpenseResponse.builder()
                .id(50L)
                .title("Client Dinner")
                .amount(new BigDecimal("3000.00"))
                .status(ExpenseStatus.DRAFT)
                .build();
    }

    @Test
    @DisplayName("Should create draft expense successfully")
    void testCreateDraftExpenseSuccess() {
        when(userRepository.findByEmail("employee@acme.com")).thenReturn(Optional.of(employee));
        when(budgetRepository.findActiveBudget(eq(1L), eq("Engineering"), any(LocalDate.class)))
                .thenReturn(Optional.of(budget));
        when(expenseMapper.toEntity(expenseRequest)).thenReturn(expense);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(expenseMapper.toResponse(expense)).thenReturn(expenseResponse);

        ExpenseResponse result = expenseService.createDraftExpense(expenseRequest, null, "employee@acme.com");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(50L);
        verify(expenseRepository).save(any(Expense.class));
        verify(auditLogService).log(eq("CREATE_EXPENSE_DRAFT"), eq("Expense"), eq(50L), eq(null), any());
    }

    @Test
    @DisplayName("Should throw BusinessException when creating expense without active departmental budget")
    void testCreateDraftExpenseNoBudget() {
        when(userRepository.findByEmail("employee@acme.com")).thenReturn(Optional.of(employee));
        when(budgetRepository.findActiveBudget(eq(1L), eq("Engineering"), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createDraftExpense(expenseRequest, null, "employee@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No active budget found for your department");
    }

    @Test
    @DisplayName("Rule 1: Should auto-approve expense < 5000 and create reimbursement")
    void testSubmitExpenseUnder5000AutoApprove() {
        expense.setAmount(new BigDecimal("4500.00"));

        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.toResponse(expense)).thenReturn(
                ExpenseResponse.builder().id(50L).status(ExpenseStatus.APPROVED).amount(new BigDecimal("4500.00")).build()
        );

        ExpenseResponse result = expenseService.submitExpense(50L, "employee@acme.com");

        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        assertThat(budget.getUtilizedAmount()).isEqualTo(new BigDecimal("24500.00")); // 20000 + 4500
        verify(budgetRepository).save(budget);
        verify(reimbursementRepository).save(any(Reimbursement.class));
        verify(auditLogService).log(eq("AUTO_APPROVE_EXPENSE"), eq("Expense"), eq(50L), any(), eq(ExpenseStatus.APPROVED.name()));
    }

    @Test
    @DisplayName("Rule 2: Should route expense between 5000 and 25000 to manager for approval")
    void testSubmitExpenseBetween5000And25000ManagerRouting() {
        expense.setAmount(new BigDecimal("15000.00"));

        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.toResponse(expense)).thenReturn(
                ExpenseResponse.builder().id(50L).status(ExpenseStatus.UNDER_REVIEW).amount(new BigDecimal("15000.00")).build()
        );

        ExpenseResponse result = expenseService.submitExpense(50L, "employee@acme.com");

        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.UNDER_REVIEW);
        verify(approvalRepository).save(argThat(approval -> 
                approval.getApprover().equals(manager) && approval.getExpense().equals(expense)
        ));
        verify(auditLogService).log(eq("SUBMIT_EXPENSE_TO_MANAGER"), eq("Expense"), eq(50L), any(), eq(ExpenseStatus.UNDER_REVIEW.name()));
    }

    @Test
    @DisplayName("Rule 3: Should route expense > 25000 to finance manager pool")
    void testSubmitExpenseOver25000FinanceRouting() {
        expense.setAmount(new BigDecimal("30000.00"));

        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.toResponse(expense)).thenReturn(
                ExpenseResponse.builder().id(50L).status(ExpenseStatus.UNDER_REVIEW).amount(new BigDecimal("30000.00")).build()
        );

        ExpenseResponse result = expenseService.submitExpense(50L, "employee@acme.com");

        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.UNDER_REVIEW);
        verify(approvalRepository).save(argThat(approval -> 
                approval.getApprover() == null && approval.getExpense().equals(expense)
        ));
        verify(auditLogService).log(eq("SUBMIT_EXPENSE_TO_FINANCE"), eq("Expense"), eq(50L), any(), eq(ExpenseStatus.UNDER_REVIEW.name()));
    }

    @Test
    @DisplayName("Should reject submission when expense exceeds remaining budget")
    void testSubmitExpenseExceedsBudget() {
        // Budget: 100,000 allocated, 20,000 utilized -> Remaining 80,000
        expense.setAmount(new BigDecimal("85000.00"));

        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.submitExpense(50L, "employee@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient departmental budget");
    }

    @Test
    @DisplayName("Should throw BusinessException when submitting another user's expense")
    void testSubmitExpenseUnauthorizedUser() {
        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.submitExpense(50L, "other@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("You are not authorized to submit this expense claim");
    }

    @Test
    @DisplayName("Should throw BusinessException when submitting an already submitted expense")
    void testSubmitAlreadySubmittedExpense() {
        expense.setStatus(ExpenseStatus.APPROVED);
        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.submitExpense(50L, "employee@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Expense is already submitted");
    }

    @Test
    @DisplayName("Should delete draft expense successfully")
    void testDeleteDraftExpenseSuccess() {
        expense.setReceiptUrl("/uploads/receipt.pdf");
        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(50L, "employee@acme.com");

        verify(storageService).delete("/uploads/receipt.pdf");
        verify(expenseRepository).delete(expense);
        verify(auditLogService).log(eq("DELETE_EXPENSE"), eq("Expense"), eq(50L), eq(expense), eq(null));
    }

    @Test
    @DisplayName("Should throw BusinessException when deleting non-draft expense")
    void testDeleteNonDraftExpense() {
        expense.setStatus(ExpenseStatus.APPROVED);
        when(expenseRepository.findById(50L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.deleteExpense(50L, "employee@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only draft expenses can be deleted");
    }
}
