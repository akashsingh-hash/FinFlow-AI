package com.finflow.ai.module.analytics;

import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.expense.Expense;
import com.finflow.ai.module.expense.ExpenseRepository;
import com.finflow.ai.module.expense.ExpenseStatus;
import com.finflow.ai.module.invoice.Invoice;
import com.finflow.ai.module.invoice.InvoiceRepository;
import com.finflow.ai.module.invoice.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public List<MonthlySpend> getMonthlySpendReport(Long companyId) {
        List<Expense> expenses = expenseRepository.findByUserCompanyId(companyId).stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED || e.getStatus() == ExpenseStatus.REIMBURSED)
                .collect(Collectors.toList());

        // Group by YearMonth
        Map<String, BigDecimal> grouped = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> {
                            java.time.LocalDateTime dt = e.getCreatedAt();
                            String monthName = dt.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                            return monthName + " " + dt.getYear();
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(Expense::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return grouped.entrySet().stream()
                .map(entry -> MonthlySpend.builder()
                        .month(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentSpend> getDepartmentSpendReport(Long companyId) {
        List<Expense> expenses = expenseRepository.findByUserCompanyId(companyId).stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED || e.getStatus() == ExpenseStatus.REIMBURSED)
                .collect(Collectors.toList());

        Map<String, BigDecimal> grouped = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getUser().getDepartment() != null ? e.getUser().getDepartment() : "General",
                        Collectors.mapping(Expense::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return grouped.entrySet().stream()
                .map(entry -> DepartmentSpend.builder()
                        .department(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorSpend> getVendorSpendReport(Long companyId) {
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId).stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PAID)
                .collect(Collectors.toList());

        Map<String, BigDecimal> grouped = invoices.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getVendor().getName(),
                        Collectors.mapping(Invoice::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return grouped.entrySet().stream()
                .map(entry -> VendorSpend.builder()
                        .vendorName(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetConsumption> getBudgetConsumptionReport(Long companyId) {
        List<Budget> budgets = budgetRepository.findByCompanyId(companyId);

        return budgets.stream()
                .map(b -> BudgetConsumption.builder()
                        .department(b.getDepartment())
                        .allocated(b.getAllocatedAmount())
                        .utilized(b.getUtilizedAmount())
                        .build())
                .collect(Collectors.toList());
    }
}
