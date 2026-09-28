package com.finflow.ai.module.budget;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.budget.dto.BudgetResponse;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private Company company;
    private Budget budget;
    private BudgetRequest budgetRequest;
    private BudgetResponse budgetResponse;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(1L).name("Acme Corp").build();

        budgetRequest = BudgetRequest.builder()
                .department("Marketing")
                .allocatedAmount(new BigDecimal("50000.00"))
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        budget = Budget.builder()
                .id(10L)
                .company(company)
                .department("Marketing")
                .allocatedAmount(new BigDecimal("50000.00"))
                .utilizedAmount(BigDecimal.ZERO)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        budgetResponse = BudgetResponse.builder()
                .id(10L)
                .department("Marketing")
                .allocatedAmount(new BigDecimal("50000.00"))
                .utilizedAmount(BigDecimal.ZERO)
                .build();
    }

    @Test
    @DisplayName("Should create budget successfully when timeframe does not overlap")
    void testCreateBudgetSuccess() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(budgetRepository.findActiveBudget(eq(1L), eq("Marketing"), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(budgetMapper.toEntity(budgetRequest)).thenReturn(budget);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);
        when(budgetMapper.toResponse(budget)).thenReturn(budgetResponse);

        BudgetResponse result = budgetService.createBudget(budgetRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(budgetRepository).save(budget);
        verify(auditLogService).log(eq("CREATE_BUDGET"), eq("Budget"), eq(10L), eq(null), any());
    }

    @Test
    @DisplayName("Should throw BusinessException when budget end date is before start date")
    void testCreateBudgetInvalidDateRange() {
        budgetRequest.setStartDate(LocalDate.of(2026, 12, 31));
        budgetRequest.setEndDate(LocalDate.of(2026, 1, 1));
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> budgetService.createBudget(budgetRequest, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Budget end date cannot be before start date");
    }

    @Test
    @DisplayName("Should throw BusinessException when overlapping budget exists for department")
    void testCreateBudgetOverlap() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(budgetRepository.findActiveBudget(1L, "Marketing", budgetRequest.getStartDate()))
                .thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.createBudget(budgetRequest, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Overlapping budget exists for this department");
    }

    @Test
    @DisplayName("Should throw BusinessException when updating budget below utilized amount")
    void testUpdateBudgetBelowUtilizedAmount() {
        budget.setUtilizedAmount(new BigDecimal("30000.00"));
        budgetRequest.setAllocatedAmount(new BigDecimal("20000.00")); // Lower than 30k

        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.updateBudget(10L, budgetRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Allocated budget cannot be lowered below currently utilized amount");
    }

    @Test
    @DisplayName("Should get active budget for department")
    void testGetActiveBudgetForDepartment() {
        when(budgetRepository.findActiveBudget(eq(1L), eq("Marketing"), any(LocalDate.class)))
                .thenReturn(Optional.of(budget));
        when(budgetMapper.toResponse(budget)).thenReturn(budgetResponse);

        BudgetResponse result = budgetService.getActiveBudgetForDepartment(1L, "Marketing");

        assertThat(result).isNotNull();
        assertThat(result.getDepartment()).isEqualTo("Marketing");
    }
}
