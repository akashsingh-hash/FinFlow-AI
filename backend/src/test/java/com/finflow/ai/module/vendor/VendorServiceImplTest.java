package com.finflow.ai.module.vendor;

import com.finflow.ai.exception.BusinessException;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.invoice.InvoiceRepository;
import com.finflow.ai.module.vendor.dto.VendorRequest;
import com.finflow.ai.module.vendor.dto.VendorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceImplTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private VendorMapper vendorMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private VendorServiceImpl vendorService;

    private Company company;
    private Vendor vendor;
    private VendorRequest vendorRequest;
    private VendorResponse vendorResponse;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(1L).name("Acme Corp").build();

        vendorRequest = VendorRequest.builder()
                .name("Office Supplies Inc")
                .email("contact@officesupplies.com")
                .phone("1234567890")
                .address("123 Main St")
                .build();

        vendor = Vendor.builder()
                .id(10L)
                .name("Office Supplies Inc")
                .email("contact@officesupplies.com")
                .company(company)
                .build();

        vendorResponse = VendorResponse.builder()
                .id(10L)
                .name("Office Supplies Inc")
                .email("contact@officesupplies.com")
                .build();
    }

    @Test
    @DisplayName("Should add vendor successfully")
    void testAddVendorSuccess() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(vendorMapper.toEntity(vendorRequest)).thenReturn(vendor);
        when(vendorRepository.save(any(Vendor.class))).thenReturn(vendor);
        when(vendorMapper.toResponse(vendor)).thenReturn(vendorResponse);

        VendorResponse result = vendorService.addVendor(vendorRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Office Supplies Inc");
        verify(vendorRepository).save(vendor);
    }

    @Test
    @DisplayName("Should delete vendor successfully when no linked invoices exist")
    void testDeleteVendorSuccess() {
        when(vendorRepository.findById(10L)).thenReturn(Optional.of(vendor));
        when(invoiceRepository.existsByVendorId(10L)).thenReturn(false);

        vendorService.deleteVendor(10L);

        verify(vendorRepository).delete(vendor);
    }

    @Test
    @DisplayName("Should throw BusinessException when deleting vendor with linked invoices")
    void testDeleteVendorWithInvoices() {
        when(vendorRepository.findById(10L)).thenReturn(Optional.of(vendor));
        when(invoiceRepository.existsByVendorId(10L)).thenReturn(true);

        assertThatThrownBy(() -> vendorService.deleteVendor(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete vendor with linked invoices");
    }
}
