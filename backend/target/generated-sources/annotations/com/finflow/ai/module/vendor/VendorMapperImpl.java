package com.finflow.ai.module.vendor;

import com.finflow.ai.module.company.Company;
import com.finflow.ai.module.vendor.dto.VendorRequest;
import com.finflow.ai.module.vendor.dto.VendorResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T13:35:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
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
        vendorResponse.address( vendor.getAddress() );
        vendorResponse.createdAt( vendor.getCreatedAt() );
        vendorResponse.email( vendor.getEmail() );
        vendorResponse.id( vendor.getId() );
        vendorResponse.name( vendor.getName() );
        vendorResponse.phone( vendor.getPhone() );
        vendorResponse.updatedAt( vendor.getUpdatedAt() );

        return vendorResponse.build();
    }

    @Override
    public Vendor toEntity(VendorRequest request) {
        if ( request == null ) {
            return null;
        }

        Vendor.VendorBuilder vendor = Vendor.builder();

        vendor.address( request.getAddress() );
        vendor.email( request.getEmail() );
        vendor.name( request.getName() );
        vendor.phone( request.getPhone() );

        return vendor.build();
    }

    @Override
    public void updateEntityFromRequest(VendorRequest request, Vendor vendor) {
        if ( request == null ) {
            return;
        }

        vendor.setAddress( request.getAddress() );
        vendor.setEmail( request.getEmail() );
        vendor.setName( request.getName() );
        vendor.setPhone( request.getPhone() );
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
