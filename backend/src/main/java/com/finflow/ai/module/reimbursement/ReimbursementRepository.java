package com.finflow.ai.module.reimbursement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
    List<Reimbursement> findByExpenseUserCompanyId(Long companyId);
    List<Reimbursement> findByExpenseUserId(Long userId);
    List<Reimbursement> findByExpenseUserCompanyIdAndStatus(Long companyId, ReimbursementStatus status);
}
