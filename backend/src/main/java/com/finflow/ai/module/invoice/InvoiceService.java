package com.finflow.ai.module.invoice;

import com.finflow.ai.module.invoice.dto.InvoiceRequest;
import com.finflow.ai.module.invoice.dto.InvoiceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InvoiceService {
    InvoiceResponse createInvoice(InvoiceRequest request, MultipartFile file, Long companyId);
    InvoiceResponse updateInvoice(Long id, InvoiceRequest request);
    InvoiceResponse markPaid(Long id);
    InvoiceResponse getInvoiceById(Long id);
    List<InvoiceResponse> getCompanyInvoices(Long companyId);
    List<InvoiceResponse> getInvoicesByStatus(Long companyId, InvoiceStatus status);
}
