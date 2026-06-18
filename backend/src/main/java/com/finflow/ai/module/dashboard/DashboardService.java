package com.finflow.ai.module.dashboard;

import com.finflow.ai.module.approval.ApprovalRepository;
import com.finflow.ai.module.approval.ApprovalStatus;
import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.expense.ExpenseRepository;
import com.finflow.ai.module.expense.ExpenseStatus;
import com.finflow.ai.module.invoice.Invoice;
import com.finflow.ai.module.invoice.InvoiceRepository;
import com.finflow.ai.module.invoice.InvoiceStatus;
import com.finflow.ai.module.reimbursement.Reimbursement;
import com.finflow.ai.module.reimbursement.ReimbursementRepository;
import com.finflow.ai.module.reimbursement.ReimbursementStatus;
import com.finflow.ai.module.vendor.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    @Transactional(readOnly = true)
    public DashboardStats getCompanyStats(Long companyId, String email, String roleName) {
        // Fetch company-scoped items
        List<Expense> expenses = expenseRepository.findByUserCompanyId(companyId);
        List<Budget> budgets = budgetRepository.findByCompanyId(companyId);
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
        List<Reimbursement> reimbursements = reimbursementRepository.findByExpenseUserCompanyId(companyId);
        long vendorCount = vendorRepository.findByCompanyId(companyId).size();

        // 1. Total Approved/Reimbursed Expenses
        BigDecimal totalExpenses = expenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED || e.getStatus() == ExpenseStatus.REIMBURSED)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Current Month's Expenses
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime monthStartDateTime = startOfMonth.atStartOfDay();
        BigDecimal monthlyExpenses = expenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED || e.getStatus() == ExpenseStatus.REIMBURSED)
                .filter(e -> e.getCreatedAt().isAfter(monthStartDateTime))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Budgets allocations vs utilization
        BigDecimal totalAllocatedBudget = budgets.stream()
                .map(Budget::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalUtilizedBudget = budgets.stream()
                .map(Budget::getUtilizedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Pending approvals (count based on user email role)
        long pendingApprovalsCount = 0;
        if (roleName.contains("MANAGER") || roleName.contains("ADMIN")) {
            // Count pending approval workflows in user's pending queue
            // We can approximate it by loading approvals
            // Let's count unassigned pool items for FINANCE_MANAGER, plus direct assignments
            pendingApprovalsCount = approvalRepository.findAll().stream()
                    .filter(a -> a.getStatus() == ApprovalStatus.PENDING)
                    .filter(a -> {
                        if (roleName.contains("FINANCE_MANAGER")) {
                            return (a.getApprover() == null && a.getExpense().getUser().getCompany().getId().equals(companyId))
                                    || (a.getApprover() != null && a.getApprover().getEmail().equals(email));
                        } else {
                            return a.getApprover() != null && a.getApprover().getEmail().equals(email);
                        }
                    })
                    .count();
        }

        // 5. Invoice metrics
        long activeInvoicesCount = invoices.stream()
                .filter(i -> i.getStatus() != InvoiceStatus.PAID)
                .count();
        BigDecimal pendingInvoiceAmount = invoices.stream()
                .filter(i -> i.getStatus() != InvoiceStatus.PAID)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Reimbursement metrics
        BigDecimal pendingReimbursementAmount = reimbursements.stream()
                .filter(r -> r.getStatus() == ReimbursementStatus.PENDING)
                .map(r -> r.getExpense().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStats.builder()
                .totalExpenses(totalExpenses)
                .monthlyExpenses(monthlyExpenses)
                .pendingApprovalsCount(pendingApprovalsCount)
                .totalAllocatedBudget(totalAllocatedBudget)
                .totalUtilizedBudget(totalUtilizedBudget)
                .vendorCount(vendorCount)
                .activeInvoicesCount(activeInvoicesCount)
                .pendingInvoiceAmount(pendingInvoiceAmount)
                .pendingReimbursementAmount(pendingReimbursementAmount)
                .build();
    }
}
