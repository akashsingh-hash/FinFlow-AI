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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReimbursementServiceImplTest {

    @Mock
    private ReimbursementRepository reimbursementRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReimbursementMapper reimbursementMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReimbursementServiceImpl reimbursementService;

    private User employee;
    private User financeManager;
    private Expense expense;
    private Reimbursement reimbursement;
    private ReimbursementPaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        employee = User.builder().id(1L).email("emp@acme.com").role(Role.EMPLOYEE).build();
        financeManager = User.builder().id(2L).email("finance@acme.com").role(Role.FINANCE_MANAGER).build();

        expense = Expense.builder()
                .id(10L)
                .amount(new BigDecimal("2500.00"))
                .user(employee)
                .status(ExpenseStatus.APPROVED)
                .build();

        reimbursement = Reimbursement.builder()
                .id(50L)
                .expense(expense)
                .status(ReimbursementStatus.PENDING)
                .build();

        paymentRequest = ReimbursementPaymentRequest.builder()
                .paymentMethod("BANK_TRANSFER")
                .paymentReference("TXN-123456")
                .build();
    }

    @Test
    @DisplayName("Should process reimbursement payment and transition expense to REIMBURSED")
    void testPayReimbursementSuccess() {
        when(reimbursementRepository.findById(50L)).thenReturn(Optional.of(reimbursement));
        when(userRepository.findByEmail("finance@acme.com")).thenReturn(Optional.of(financeManager));
        when(reimbursementRepository.save(any(Reimbursement.class))).thenReturn(reimbursement);
        when(reimbursementMapper.toResponse(reimbursement)).thenReturn(
                ReimbursementResponse.builder().id(50L).status(ReimbursementStatus.PAID).build()
        );

        ReimbursementResponse response = reimbursementService.payReimbursement(50L, paymentRequest, "finance@acme.com");

        assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementStatus.PAID);
        assertThat(reimbursement.getPaymentReference()).isEqualTo("TXN-123456");
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REIMBURSED);
        verify(expenseRepository).save(expense);
    }

    @Test
    @DisplayName("Should throw BusinessException when non-finance user attempts to record payment")
    void testPayReimbursementUnauthorizedRole() {
        when(reimbursementRepository.findById(50L)).thenReturn(Optional.of(reimbursement));
        when(userRepository.findByEmail("emp@acme.com")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> reimbursementService.payReimbursement(50L, paymentRequest, "emp@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only users with FINANCE_MANAGER or ADMIN role can record payouts");
    }

    @Test
    @DisplayName("Should throw BusinessException when reimbursement is already PAID")
    void testPayReimbursementAlreadyPaid() {
        reimbursement.setStatus(ReimbursementStatus.PAID);
        when(reimbursementRepository.findById(50L)).thenReturn(Optional.of(reimbursement));

        assertThatThrownBy(() -> reimbursementService.payReimbursement(50L, paymentRequest, "finance@acme.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("This reimbursement claim has already been paid");
    }
}
