package com.finflow.ai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.invoice.Invoice;
import com.finflow.ai.module.invoice.InvoiceRepository;
import com.finflow.ai.module.invoice.InvoiceStatus;
import com.finflow.ai.module.invoice.dto.InvoiceRequest;
import com.finflow.ai.module.vendor.Vendor;
import com.finflow.ai.module.vendor.VendorRepository;
import com.finflow.ai.module.vendor.dto.VendorRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VendorAndInvoiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company;
    private Vendor vendor;

    @BeforeEach
    void setUp() {
        company = companyRepository.findByName("Acme Corporation").orElseGet(() ->
                companyRepository.save(Company.builder().name("Acme Corporation").taxId("TAX-ACME").build())
        );

        vendor = vendorRepository.findByCompanyId(company.getId()).stream()
                .filter(v -> "AWS Cloud Services".equals(v.getName()))
                .findFirst()
                .orElseGet(() -> vendorRepository.save(Vendor.builder()
                        .name("AWS Cloud Services")
                        .email("billing@aws.amazon.com")
                        .company(company)
                        .build())
                );
    }

    @Test
    @WithUserDetails(value = "admin@acme.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Integration: Add vendor -> Mark invoice paid")
    void testVendorAndInvoiceWorkflow() throws Exception {
        // 1. Add new vendor
        VendorRequest vendorReq = VendorRequest.builder()
                .name("GitHub Enterprise")
                .email("billing@github.com")
                .phone("+1-800-555-0199")
                .address("88 Colin P Kelly Jr St, SF")
                .build();

        mockMvc.perform(post("/vendors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vendorReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("GitHub Enterprise"));

        // 2. Mark existing invoice as paid
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-TEST-999")
                .amount(new BigDecimal("4200.00"))
                .dueDate(LocalDate.now().plusDays(15))
                .status(InvoiceStatus.CREATED)
                .vendor(vendor)
                .company(company)
                .build();
        invoice = invoiceRepository.save(invoice);

        mockMvc.perform(patch("/invoices/" + invoice.getId() + "/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAID"));

        Invoice reloaded = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }
}
