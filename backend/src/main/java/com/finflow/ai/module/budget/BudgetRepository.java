package com.finflow.ai.module.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByCompanyId(Long companyId);
    List<Budget> findByCompanyIdAndDepartment(Long companyId, String department);

    @Query("SELECT b FROM Budget b WHERE b.company.id = :companyId " +
           "AND b.department = :department " +
           "AND :date BETWEEN b.startDate AND b.endDate")
    Optional<Budget> findActiveBudget(
            @Param("companyId") Long companyId,
            @Param("department") String department,
            @Param("date") LocalDate date
    );
}
