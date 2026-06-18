package com.finflow.ai.module.company;

import com.finflow.ai.module.company.dto.CompanyRequest;
import com.finflow.ai.module.company.dto.CompanyResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-18T13:35:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CompanyMapperImpl implements CompanyMapper {

    @Override
    public CompanyResponse toResponse(Company company) {
        if ( company == null ) {
            return null;
        }

        CompanyResponse.CompanyResponseBuilder companyResponse = CompanyResponse.builder();

        companyResponse.address( company.getAddress() );
        companyResponse.createdAt( company.getCreatedAt() );
        companyResponse.id( company.getId() );
        companyResponse.name( company.getName() );
        companyResponse.taxId( company.getTaxId() );
        companyResponse.updatedAt( company.getUpdatedAt() );

        return companyResponse.build();
    }

    @Override
    public Company toEntity(CompanyRequest request) {
        if ( request == null ) {
            return null;
        }

        Company.CompanyBuilder company = Company.builder();

        company.address( request.getAddress() );
        company.name( request.getName() );
        company.taxId( request.getTaxId() );

        return company.build();
    }

    @Override
    public void updateEntityFromRequest(CompanyRequest request, Company company) {
        if ( request == null ) {
            return;
        }

        company.setAddress( request.getAddress() );
        company.setName( request.getName() );
        company.setTaxId( request.getTaxId() );
    }
}
