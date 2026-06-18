package com.finflow.ai.module.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private BigDecimal totalExpenses;
    private BigDecimal monthlyExpenses;
    private long pendingApprovalsCount;
    private BigDecimal totalAllocatedBudget;
    private BigDecimal totalUtilizedBudget;
    private long vendorCount;
    private long activeInvoicesCount;
    private BigDecimal pendingInvoiceAmount;
    private BigDecimal pendingReimbursementAmount;
}
