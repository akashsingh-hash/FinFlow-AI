package com.finflow.ai.module.budget;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.budget.dto.BudgetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public BudgetResponse createBudget(BudgetRequest request, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("Budget end date cannot be before start date");
        }

        // Check if there is an overlapping active budget
        Optional<Budget> overlapStart = budgetRepository.findActiveBudget(companyId, request.getDepartment(), request.getStartDate());
        Optional<Budget> overlapEnd = budgetRepository.findActiveBudget(companyId, request.getDepartment(), request.getEndDate());
        if (overlapStart.isPresent() || overlapEnd.isPresent()) {
            throw new BusinessException("Overlapping budget exists for this department in the selected timeframe");
        }

        Budget budget = budgetMapper.toEntity(request);
        budget.setCompany(company);
        budget.setUtilizedAmount(BigDecimal.ZERO);

        Budget saved = budgetRepository.save(budget);
        auditLogService.log("CREATE_BUDGET", "Budget", saved.getId(), null, saved);

        return budgetMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("Budget end date cannot be before start date");
        }

        Budget oldBudgetCopy = Budget.builder()
                .id(budget.getId())
                .department(budget.getDepartment())
                .allocatedAmount(budget.getAllocatedAmount())
                .utilizedAmount(budget.getUtilizedAmount())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .build();

        if (request.getAllocatedAmount().compareTo(budget.getUtilizedAmount()) < 0) {
            throw new BusinessException("Allocated budget cannot be lowered below currently utilized amount: " + budget.getUtilizedAmount());
        }

        budgetMapper.updateEntityFromRequest(request, budget);
        Budget updated = budgetRepository.save(budget);

        auditLogService.log("UPDATE_BUDGET", "Budget", updated.getId(), oldBudgetCopy, updated);

        return budgetMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getCompanyBudgets(Long companyId) {
        return budgetRepository.findByCompanyId(companyId).stream()
                .map(budgetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        return budgetMapper.toResponse(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getActiveBudgetForDepartment(Long companyId, String department) {
        Budget budget = budgetRepository.findActiveBudget(companyId, department, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("No active budget found for department: " + department));
        return budgetMapper.toResponse(budget);
    }
}
