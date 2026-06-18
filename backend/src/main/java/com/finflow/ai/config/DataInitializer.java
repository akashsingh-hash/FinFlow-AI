package com.finflow.ai.config;

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
import com.finflow.ai.module.invoice.Invoice;
import com.finflow.ai.module.invoice.InvoiceRepository;
import com.finflow.ai.module.invoice.InvoiceStatus;
import com.finflow.ai.module.reimbursement.Reimbursement;
import com.finflow.ai.module.reimbursement.ReimbursementRepository;
import com.finflow.ai.module.reimbursement.ReimbursementStatus;
import com.finflow.ai.module.user.Role;
import com.finflow.ai.module.user.User;
import com.finflow.ai.module.user.UserRepository;
import com.finflow.ai.module.user.UserStatus;
import com.finflow.ai.module.vendor.Vendor;
import com.finflow.ai.module.vendor.VendorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Database is empty. Initializing seed data...");

            // 1. Create Company
            Company company = Company.builder()
                    .name("Acme Corporation")
                    .taxId("US-1234567-ACME")
                    .address("100 Innovation Way, Silicon Valley, CA")
                    .build();
            company = companyRepository.save(company);

            // 2. Create Users for all 5 Roles
            String commonPassword = passwordEncoder.encode("password123");

            User admin = User.builder()
                    .email("admin@acme.com")
                    .passwordHash(commonPassword)
                    .firstName("Alice")
                    .lastName("Smith")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .department("Executive")
                    .company(company)
                    .build();
            admin = userRepository.save(admin);

            User financeManager = User.builder()
                    .email("finance@acme.com")
                    .passwordHash(commonPassword)
                    .firstName("Bob")
                    .lastName("Jones")
                    .role(Role.FINANCE_MANAGER)
                    .status(UserStatus.ACTIVE)
                    .department("Finance")
                    .company(company)
                    .build();
            financeManager = userRepository.save(financeManager);

            User manager = User.builder()
                    .email("manager@acme.com")
                    .passwordHash(commonPassword)
                    .firstName("Charlie")
                    .lastName("Brown")
                    .role(Role.MANAGER)
                    .status(UserStatus.ACTIVE)
                    .department("Engineering")
                    .company(company)
                    .build();
            manager = userRepository.save(manager);

            User employee = User.builder()
                    .email("employee@acme.com")
                    .passwordHash(commonPassword)
                    .firstName("Edward")
                    .lastName("Miller")
                    .role(Role.EMPLOYEE)
                    .status(UserStatus.ACTIVE)
                    .department("Engineering")
                    .company(company)
                    .manager(manager) // Assigned Manager
                    .build();
            employee = userRepository.save(employee);

            User auditor = User.builder()
                    .email("auditor@acme.com")
                    .passwordHash(commonPassword)
                    .firstName("Audrey")
                    .lastName("Davis")
                    .role(Role.AUDITOR)
                    .status(UserStatus.ACTIVE)
                    .department("Audit")
                    .company(company)
                    .build();
            auditor = userRepository.save(auditor);

            // 3. Create Department Budgets
            Budget engineeringBudget = Budget.builder()
                    .department("Engineering")
                    .allocatedAmount(new BigDecimal("100000.00"))
                    .utilizedAmount(new BigDecimal("240.00")) // For the approved expense
                    .startDate(LocalDate.now().minusMonths(3))
                    .endDate(LocalDate.now().plusMonths(9))
                    .company(company)
                    .build();
            engineeringBudget = budgetRepository.save(engineeringBudget);

            Budget financeBudget = Budget.builder()
                    .department("Finance")
                    .allocatedAmount(new BigDecimal("50000.00"))
                    .utilizedAmount(BigDecimal.ZERO)
                    .startDate(LocalDate.now().minusMonths(3))
                    .endDate(LocalDate.now().plusMonths(9))
                    .company(company)
                    .build();
            budgetRepository.save(financeBudget);

            // 4. Create Vendors
            Vendor awsVendor = Vendor.builder()
                    .name("Amazon Web Services")
                    .email("billing@aws.amazon.com")
                    .phone("+1-800-AWS")
                    .address("Seattle, WA")
                    .company(company)
                    .build();
            awsVendor = vendorRepository.save(awsVendor);

            Vendor slackVendor = Vendor.builder()
                    .name("Slack Technologies")
                    .email("billing@slack.com")
                    .phone("+1-888-SLACK")
                    .address("San Francisco, CA")
                    .company(company)
                    .build();
            slackVendor = vendorRepository.save(slackVendor);

            // 5. Create Invoices
            Invoice invoice1 = Invoice.builder()
                    .invoiceNumber("INV-AWS-2026-001")
                    .amount(new BigDecimal("1500.00"))
                    .dueDate(LocalDate.now().plusDays(15))
                    .status(InvoiceStatus.CREATED)
                    .vendor(awsVendor)
                    .company(company)
                    .build();
            invoiceRepository.save(invoice1);

            Invoice invoice2 = Invoice.builder()
                    .invoiceNumber("INV-SLACK-2026-001")
                    .amount(new BigDecimal("3200.00"))
                    .dueDate(LocalDate.now().minusDays(5)) // Overdue
                    .status(InvoiceStatus.OVERDUE)
                    .vendor(slackVendor)
                    .company(company)
                    .build();
            invoiceRepository.save(invoice2);

            // 6. Create Expenses
            // Draft Expense
            Expense expenseDraft = Expense.builder()
                    .title("Office Stationery and Supplies")
                    .amount(new BigDecimal("120.00"))
                    .category("Office Supplies")
                    .description("Bought notebooks, pens, and desk organizers.")
                    .status(ExpenseStatus.DRAFT)
                    .user(employee)
                    .budget(engineeringBudget)
                    .build();
            expenseRepository.save(expenseDraft);

            // Approved Expense & Reimbursement
            Expense expenseApproved = Expense.builder()
                    .title("Client Dinner - Tech Innovation Hub")
                    .amount(new BigDecimal("240.00"))
                    .category("Meals & Entertainment")
                    .description("Dinner with client lead from Alpha Tech Corp.")
                    .status(ExpenseStatus.APPROVED)
                    .user(employee)
                    .budget(engineeringBudget)
                    .build();
            expenseApproved = expenseRepository.save(expenseApproved);

            Reimbursement reimbursement = Reimbursement.builder()
                    .expense(expenseApproved)
                    .status(ReimbursementStatus.PENDING)
                    .build();
            reimbursementRepository.save(reimbursement);

            // Under Review Expense (routes to Charlie manager)
            Expense expenseReview = Expense.builder()
                    .title("Enterprise Software Licenses")
                    .amount(new BigDecimal("12000.00"))
                    .category("Software")
                    .description("JetBrains and Docker licenses for the Engineering team.")
                    .status(ExpenseStatus.UNDER_REVIEW)
                    .user(employee)
                    .budget(engineeringBudget)
                    .build();
            expenseReview = expenseRepository.save(expenseReview);

            ApprovalWorkflow workflowStep = ApprovalWorkflow.builder()
                    .expense(expenseReview)
                    .approver(manager)
                    .status(ApprovalStatus.PENDING)
                    .build();
            approvalRepository.save(workflowStep);

            log.info("Database seeding completed successfully. Created 5 users, 2 budgets, 2 vendors, 2 invoices, and 3 expenses.");
        } else {
            log.info("Database already contains data. Skipping seeding.");
        }
    }
}
