package com.finflow.ai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.ai.module.approval.ApprovalRepository;
import com.finflow.ai.module.approval.ApprovalStatus;
import com.finflow.ai.module.approval.ApprovalWorkflow;
import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.expense.ExpenseRepository;
import com.finflow.ai.module.expense.ExpenseStatus;
import com.finflow.ai.module.expense.dto.ExpenseRequest;
import com.finflow.ai.module.reimbursement.Reimbursement;
import com.finflow.ai.module.reimbursement.ReimbursementRepository;
import com.finflow.ai.module.reimbursement.ReimbursementStatus;
import com.finflow.ai.module.reimbursement.dto.ReimbursementPaymentRequest;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserRepository;
import com.finflow.ai.module.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    private Company company;
    private User manager;
    private User employee;
    private User financeUser;
    private Budget budget;

    @BeforeEach
    void setupData() {
        company = companyRepository.findByName("Acme Corporation").orElseGet(() ->
                companyRepository.save(Company.builder().name("Acme Corporation").taxId("TAX-ACME").build())
        );

        manager = userRepository.findByEmail("manager@acme.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("manager@acme.com")
                        .passwordHash("hashed")
                        .firstName("Bob")
                        .lastName("Manager")
                        .role(Role.MANAGER)
                        .status(UserStatus.ACTIVE)
                        .company(company)
                        .department("Engineering")
                        .build())
        );

        employee = userRepository.findByEmail("employee@acme.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("employee@acme.com")
                        .passwordHash("hashed")
                        .firstName("Alice")
                        .lastName("Employee")
                        .role(Role.EMPLOYEE)
                        .status(UserStatus.ACTIVE)
                        .company(company)
                        .department("Engineering")
                        .manager(manager)
                        .build())
        );
        if (employee.getManager() == null) {
            employee.setManager(manager);
            employee = userRepository.save(employee);
        }

        financeUser = userRepository.findByEmail("finance@acme.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("finance@acme.com")
                        .passwordHash("hashed")
                        .firstName("Frank")
                        .lastName("Finance")
                        .role(Role.FINANCE_MANAGER)
                        .status(UserStatus.ACTIVE)
                        .company(company)
                        .department("Finance")
                        .build())
        );

        budget = budgetRepository.findActiveBudget(company.getId(), "Engineering", LocalDate.now()).orElseGet(() ->
                budgetRepository.save(Budget.builder()
                        .company(company)
                        .department("Engineering")
                        .allocatedAmount(new BigDecimal("500000.00"))
                        .utilizedAmount(new BigDecimal("10000.00"))
                        .startDate(LocalDate.now().minusMonths(1))
                        .endDate(LocalDate.now().plusMonths(6))
                        .build())
        );
    }

    @Test
    @WithUserDetails(value = "employee@acme.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Integration: Submit expense < 5000 -> Auto Approved -> Reimbursement created")
    void testExpenseAutoApprovalFlow() throws Exception {
        Expense expense = Expense.builder()
                .title("Office Stationery")
                .amount(new BigDecimal("1500.00"))
                .category("Supplies")
                .user(employee)
                .budget(budget)
                .status(ExpenseStatus.DRAFT)
                .build();
        expense = expenseRepository.save(expense);

        // Submit expense
        mockMvc.perform(post("/expenses/" + expense.getId() + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        Expense updatedExpense = expenseRepository.findById(expense.getId()).orElseThrow();
        assertThat(updatedExpense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);

        List<Reimbursement> reimbursements = reimbursementRepository.findByExpenseUserId(employee.getId());
        assertThat(reimbursements).anyMatch(r -> r.getExpense().getId().equals(updatedExpense.getId())
                && r.getStatus() == ReimbursementStatus.PENDING);
    }

    @Test
    @WithUserDetails(value = "employee@acme.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Integration: Submit expense between 5000 and 25000 -> Routes to Manager as UNDER_REVIEW")
    void testExpenseManagerApprovalFlow() throws Exception {
        Expense expense = Expense.builder()
                .title("Conference Flight Ticket")
                .amount(new BigDecimal("12000.00"))
                .category("Travel")
                .user(employee)
                .budget(budget)
                .status(ExpenseStatus.DRAFT)
                .build();
        expense = expenseRepository.save(expense);

        // Submit expense
        mockMvc.perform(post("/expenses/" + expense.getId() + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        Expense updatedExpense = expenseRepository.findById(expense.getId()).orElseThrow();
        assertThat(updatedExpense.getStatus()).isEqualTo(ExpenseStatus.UNDER_REVIEW);

        List<ApprovalWorkflow> approvals = approvalRepository.findByExpenseId(expense.getId());
        assertThat(approvals).hasSize(1);
        assertThat(approvals.get(0).getApprover().getId()).isEqualTo(manager.getId());
        assertThat(approvals.get(0).getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }
}
