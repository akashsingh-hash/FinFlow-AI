package com.finflow.ai.module.invoice;

import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.invoice.dto.InvoiceRequest;
import com.finflow.ai.module.invoice.dto.InvoiceResponse;
import com.finflow.ai.module.vendor.Vendor;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:36+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class InvoiceMapperImpl implements InvoiceMapper {

    @Override
    public InvoiceResponse toResponse(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }

        InvoiceResponse.InvoiceResponseBuilder invoiceResponse = InvoiceResponse.builder();

        invoiceResponse.companyId( invoiceCompanyId( invoice ) );
        invoiceResponse.vendorId( invoiceVendorId( invoice ) );
        invoiceResponse.vendorName( invoiceVendorName( invoice ) );
        invoiceResponse.id( invoice.getId() );
        invoiceResponse.invoiceNumber( invoice.getInvoiceNumber() );
        invoiceResponse.amount( invoice.getAmount() );
        invoiceResponse.dueDate( invoice.getDueDate() );
        invoiceResponse.status( invoice.getStatus() );
        invoiceResponse.fileUrl( invoice.getFileUrl() );
        invoiceResponse.createdAt( invoice.getCreatedAt() );
        invoiceResponse.updatedAt( invoice.getUpdatedAt() );

        return invoiceResponse.build();
    }

    @Override
    public Invoice toEntity(InvoiceRequest request) {
        if ( request == null ) {
            return null;
        }

        Invoice.InvoiceBuilder invoice = Invoice.builder();

        invoice.invoiceNumber( request.getInvoiceNumber() );
        invoice.amount( request.getAmount() );
        invoice.dueDate( request.getDueDate() );

        return invoice.build();
    }

    @Override
    public void updateEntityFromRequest(InvoiceRequest request, Invoice invoice) {
        if ( request == null ) {
            return;
        }

        invoice.setInvoiceNumber( request.getInvoiceNumber() );
        invoice.setAmount( request.getAmount() );
        invoice.setDueDate( request.getDueDate() );
    }

    private Long invoiceCompanyId(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Company company = invoice.getCompany();
        if ( company == null ) {
            return null;
        }
        Long id = company.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long invoiceVendorId(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Vendor vendor = invoice.getVendor();
        if ( vendor == null ) {
            return null;
        }
        Long id = vendor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String invoiceVendorName(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Vendor vendor = invoice.getVendor();
        if ( vendor == null ) {
            return null;
        }
        String name = vendor.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
