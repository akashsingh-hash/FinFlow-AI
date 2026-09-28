package com.finflow.ai.module.invoice;

import com.finflow.ai.common.storage.StorageService;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.invoice.dto.InvoiceRequest;
import com.finflow.ai.module.invoice.dto.InvoiceResponse;
import com.finflow.ai.module.vendor.Vendor;
import com.finflow.ai.module.vendor.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Company company;
    private Vendor vendor;
    private Invoice invoice;
    private InvoiceRequest invoiceRequest;
    private InvoiceResponse invoiceResponse;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(1L).name("Acme Corp").build();
        vendor = Vendor.builder().id(2L).name("Cloud Services").build();

        invoiceRequest = InvoiceRequest.builder()
                .invoiceNumber("INV-2026-001")
                .vendorId(2L)
                .amount(new BigDecimal("1500.00"))
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        invoice = Invoice.builder()
                .id(100L)
                .invoiceNumber("INV-2026-001")
                .amount(new BigDecimal("1500.00"))
                .company(company)
                .vendor(vendor)
                .status(InvoiceStatus.CREATED)
                .build();

        invoiceResponse = InvoiceResponse.builder()
                .id(100L)
                .invoiceNumber("INV-2026-001")
                .amount(new BigDecimal("1500.00"))
                .status(InvoiceStatus.CREATED)
                .build();
    }

    @Test
    @DisplayName("Should create invoice with CREATED status when due date is in the future")
    void testCreateInvoiceFutureDueDate() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(vendorRepository.findById(2L)).thenReturn(Optional.of(vendor));
        when(invoiceMapper.toEntity(invoiceRequest)).thenReturn(invoice);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toResponse(invoice)).thenReturn(invoiceResponse);

        InvoiceResponse response = invoiceService.createInvoice(invoiceRequest, null, 1L);

        assertThat(response).isNotNull();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CREATED);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Should mark invoice as PAID successfully")
    void testMarkPaid() {
        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(invoiceMapper.toResponse(invoice)).thenReturn(
                InvoiceResponse.builder().id(100L).status(InvoiceStatus.PAID).build()
        );

        InvoiceResponse response = invoiceService.markPaid(100L);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        verify(auditLogService).log(eq("PAY_INVOICE"), eq("Invoice"), eq(100L), any(), eq("PAID"));
    }
}
