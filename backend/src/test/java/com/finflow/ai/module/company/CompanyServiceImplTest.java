package com.finflow.ai.module.company;

import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.dto.CompanyRequest;
import com.finflow.ai.module.company.dto.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company company;
    private CompanyRequest companyRequest;
    private CompanyResponse companyResponse;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(1L)
                .name("Acme Corp")
                .taxId("TAX123")
                .address("100 Innovation Way")
                .build();

        companyRequest = CompanyRequest.builder()
                .name("Acme Corp")
                .taxId("TAX123")
                .address("100 Innovation Way")
                .build();

        companyResponse = CompanyResponse.builder()
                .id(1L)
                .name("Acme Corp")
                .taxId("TAX123")
                .address("100 Innovation Way")
                .build();
    }

    @Test
    @DisplayName("Should create company successfully")
    void testCreateCompany() {
        when(companyMapper.toEntity(companyRequest)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toResponse(company)).thenReturn(companyResponse);

        CompanyResponse result = companyService.createCompany(companyRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Acme Corp");
        verify(companyRepository).save(company);
        verify(auditLogService).log(eq("CREATE_COMPANY"), eq("Company"), eq(1L), eq(null), any());
    }

    @Test
    @DisplayName("Should get company by ID successfully")
    void testGetCompanyByIdSuccess() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyMapper.toResponse(company)).thenReturn(companyResponse);

        CompanyResponse result = companyService.getCompanyById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when company ID does not exist")
    void testGetCompanyByIdNotFound() {
        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanyById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found with ID: 999");
    }
}
