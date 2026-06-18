package com.finflow.ai.module.vendor;

import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.company.CompanyRepository;
import com.finflow.ai.module.invoice.InvoiceRepository;
import com.finflow.ai.module.vendor.dto.VendorRequest;
import com.finflow.ai.module.vendor.dto.VendorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public VendorResponse addVendor(VendorRequest request, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Vendor vendor = vendorMapper.toEntity(request);
        vendor.setCompany(company);

        Vendor saved = vendorRepository.save(vendor);
        auditLogService.log("ADD_VENDOR", "Vendor", saved.getId(), null, saved);

        return vendorMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VendorResponse updateVendor(Long id, VendorRequest request) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        Vendor oldVendorCopy = Vendor.builder()
                .id(vendor.getId())
                .name(vendor.getName())
                .email(vendor.getEmail())
                .phone(vendor.getPhone())
                .address(vendor.getAddress())
                .build();

        vendorMapper.updateEntityFromRequest(request, vendor);
        Vendor updated = vendorRepository.save(vendor);

        auditLogService.log("UPDATE_VENDOR", "Vendor", updated.getId(), oldVendorCopy, updated);

        return vendorMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (invoiceRepository.existsByVendorId(id)) {
            throw new com.finflow.ai.exception.BusinessException("Cannot delete vendor with linked invoices. Please resolve the invoices first.");
        }

        vendorRepository.delete(vendor);
        auditLogService.log("DELETE_VENDOR", "Vendor", id, vendor, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorResponse> getCompanyVendors(Long companyId) {
        return vendorRepository.findByCompanyId(companyId).stream()
                .map(vendorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        return vendorMapper.toResponse(vendor);
    }
}
