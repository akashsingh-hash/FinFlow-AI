package com.finflow.ai.module.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCompanyId(Long companyId);
    List<Invoice> findByVendorId(Long vendorId);
    List<Invoice> findByCompanyIdAndStatus(Long companyId, InvoiceStatus status);
    boolean existsByVendorId(Long vendorId);
}
