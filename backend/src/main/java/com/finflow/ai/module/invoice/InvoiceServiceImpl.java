package com.finflow.ai.module.invoice;

import com.finflow.ai.common.storage.StorageService;
import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.vendor.Vendor;
import com.finflow.ai.module.vendor.VendorRepository;
import com.finflow.ai.module.invoice.dto.InvoiceRequest;
import com.finflow.ai.module.invoice.dto.InvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Autowired
    private StorageService storageService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request, MultipartFile file, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = storageService.store(file, "invoices");
        }

        Invoice invoice = invoiceMapper.toEntity(request);
        invoice.setCompany(company);
        invoice.setVendor(vendor);
        invoice.setFileUrl(fileUrl);
        
        // Determine status based on due date
        if (request.getDueDate().isBefore(LocalDate.now())) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        } else {
            invoice.setStatus(InvoiceStatus.CREATED);
        }

        Invoice saved = invoiceRepository.save(invoice);
        auditLogService.log("CREATE_INVOICE", "Invoice", saved.getId(), null, saved);

        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        Invoice oldInvoiceCopy = Invoice.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getAmount())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .build();

        invoiceMapper.updateEntityFromRequest(request, invoice);
        invoice.setVendor(vendor);

        Invoice updated = invoiceRepository.save(invoice);
        auditLogService.log("UPDATE_INVOICE", "Invoice", updated.getId(), oldInvoiceCopy, updated);

        return invoiceMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public InvoiceResponse markPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        InvoiceStatus oldStatus = invoice.getStatus();
        invoice.setStatus(InvoiceStatus.PAID);
        Invoice updated = invoiceRepository.save(invoice);

        auditLogService.log("PAY_INVOICE", "Invoice", updated.getId(), oldStatus.name(), updated.getStatus().name());

        return invoiceMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getCompanyInvoices(Long companyId) {
        return invoiceRepository.findByCompanyId(companyId).stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(Long companyId, InvoiceStatus status) {
        return invoiceRepository.findByCompanyIdAndStatus(companyId, status).stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }
}
