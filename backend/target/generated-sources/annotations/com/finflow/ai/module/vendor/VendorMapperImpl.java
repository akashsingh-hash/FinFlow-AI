package com.finflow.ai.module.vendor;

import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.vendor.dto.VendorRequest;
import com.finflow.ai.module.vendor.dto.VendorResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:40+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class VendorMapperImpl implements VendorMapper {

    @Override
    public VendorResponse toResponse(Vendor vendor) {
        if ( vendor == null ) {
            return null;
        }

        VendorResponse.VendorResponseBuilder vendorResponse = VendorResponse.builder();

        vendorResponse.companyId( vendorCompanyId( vendor ) );
        vendorResponse.id( vendor.getId() );
        vendorResponse.name( vendor.getName() );
        vendorResponse.email( vendor.getEmail() );
        vendorResponse.phone( vendor.getPhone() );
        vendorResponse.address( vendor.getAddress() );
        vendorResponse.createdAt( vendor.getCreatedAt() );
        vendorResponse.updatedAt( vendor.getUpdatedAt() );

        return vendorResponse.build();
    }

    @Override
    public Vendor toEntity(VendorRequest request) {
        if ( request == null ) {
            return null;
        }

        Vendor.VendorBuilder vendor = Vendor.builder();

        vendor.name( request.getName() );
        vendor.email( request.getEmail() );
        vendor.phone( request.getPhone() );
        vendor.address( request.getAddress() );

        return vendor.build();
    }

    @Override
    public void updateEntityFromRequest(VendorRequest request, Vendor vendor) {
        if ( request == null ) {
            return;
        }

        vendor.setName( request.getName() );
        vendor.setEmail( request.getEmail() );
        vendor.setPhone( request.getPhone() );
        vendor.setAddress( request.getAddress() );
    }

    private Long vendorCompanyId(Vendor vendor) {
        if ( vendor == null ) {
            return null;
        }
        Company company = vendor.getCompany();
        if ( company == null ) {
            return null;
        }
        Long id = company.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
