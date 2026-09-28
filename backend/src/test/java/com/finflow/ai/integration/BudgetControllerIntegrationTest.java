package com.finflow.ai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.ai.module.budget.Budget;
import com.finflow.ai.module.budget.BudgetRepository;
import com.finflow.ai.module.budget.dto.BudgetRequest;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BudgetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private Company company;

    @BeforeEach
    void setUp() {
        company = companyRepository.findByName("Acme Corporation").orElseGet(() ->
                companyRepository.save(Company.builder().name("Acme Corporation").taxId("TAX-ACME").build())
        );
    }

    @Test
    @WithUserDetails(value = "admin@acme.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Integration: Create budget successfully for new department")
    void testCreateBudgetSuccess() throws Exception {
        BudgetRequest request = BudgetRequest.builder()
                .department("Legal")
                .allocatedAmount(new BigDecimal("75000.00"))
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(12))
                .build();

        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.department").value("Legal"))
                .andExpect(jsonPath("$.data.allocatedAmount").value(75000.00));
    }

    @Test
    @WithUserDetails(value = "admin@acme.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Integration: Reject budget when end date is before start date")
    void testCreateBudgetInvalidDates() throws Exception {
        BudgetRequest request = BudgetRequest.builder()
                .department("Operations")
                .allocatedAmount(new BigDecimal("30000.00"))
                .startDate(LocalDate.now().plusMonths(6))
                .endDate(LocalDate.now().plusMonths(1)) // End is before start
                .build();

        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Budget end date cannot be before start date"));
    }
}
