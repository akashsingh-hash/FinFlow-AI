package com.finflow.ai.module.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserCompanyId(Long companyId);
    List<Expense> findByUserManagerId(Long managerId);
    List<Expense> findByBudgetId(Long budgetId);
    List<Expense> findByUserCompanyIdAndStatus(Long companyId, ExpenseStatus status);
}
