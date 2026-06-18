package com.finflow.ai.module.invoice;

import com.finflow.ai.common.ApiResponse;
import com.finflow.ai.module.invoice.dto.InvoiceRequest;
import com.finflow.ai.module.invoice.dto.InvoiceResponse;
import com.finflow.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'MANAGER')")
    public ApiResponse<InvoiceResponse> createInvoice(
            @RequestPart("invoice") @Valid InvoiceRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        InvoiceResponse response = invoiceService.createInvoice(request, file, companyId);
        return ApiResponse.success(response, "Invoice created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'MANAGER')")
    public ApiResponse<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ApiResponse.success(response, "Invoice updated successfully");
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public ApiResponse<InvoiceResponse> markPaid(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.markPaid(id);
        return ApiResponse.success(response, "Invoice marked as paid");
    }

    @GetMapping
    public ApiResponse<List<InvoiceResponse>> getCompanyInvoices(@AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<InvoiceResponse> responses = invoiceService.getCompanyInvoices(companyId);
        return ApiResponse.success(responses, "Invoices retrieved successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ApiResponse.success(response, "Invoice retrieved successfully");
    }

    @GetMapping("/status")
    public ApiResponse<List<InvoiceResponse>> getInvoicesByStatus(
            @RequestParam InvoiceStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long companyId = principal.getUser().getCompany().getId();
        List<InvoiceResponse> responses = invoiceService.getInvoicesByStatus(companyId, status);
        return ApiResponse.success(responses, "Invoices with status " + status + " retrieved successfully");
    }
}
