package com.finflow.ai.module.approval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<ApprovalWorkflow, Long> {
    List<ApprovalWorkflow> findByApproverId(Long approverId);
    List<ApprovalWorkflow> findByExpenseId(Long expenseId);
    List<ApprovalWorkflow> findByApproverIdAndStatus(Long approverId, ApprovalStatus status);

    @Query("SELECT a FROM ApprovalWorkflow a WHERE a.status = :status " +
           "AND a.approver IS NULL AND a.expense.user.company.id = :companyId")
    List<ApprovalWorkflow> findUnassignedPoolApprovals(
            @Param("companyId") Long companyId,
            @Param("status") ApprovalStatus status
    );
}
