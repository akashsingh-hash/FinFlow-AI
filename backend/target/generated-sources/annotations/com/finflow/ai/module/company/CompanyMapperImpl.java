package com.finflow.ai.module.company;

import com.finflow.ai.module.company.dto.CompanyRequest;
import com.finflow.ai.module.company.dto.CompanyResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:57:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24 (Oracle Corporation)"
)
@Component
public class CompanyMapperImpl implements CompanyMapper {

    @Override
    public CompanyResponse toResponse(Company company) {
        if ( company == null ) {
            return null;
        }

        CompanyResponse.CompanyResponseBuilder companyResponse = CompanyResponse.builder();

        companyResponse.id( company.getId() );
        companyResponse.name( company.getName() );
        companyResponse.taxId( company.getTaxId() );
        companyResponse.address( company.getAddress() );
        companyResponse.createdAt( company.getCreatedAt() );
        companyResponse.updatedAt( company.getUpdatedAt() );

        return companyResponse.build();
    }

    @Override
    public Company toEntity(CompanyRequest request) {
        if ( request == null ) {
            return null;
        }

        Company.CompanyBuilder company = Company.builder();

        company.name( request.getName() );
        company.taxId( request.getTaxId() );
        company.address( request.getAddress() );

        return company.build();
    }

    @Override
    public void updateEntityFromRequest(CompanyRequest request, Company company) {
        if ( request == null ) {
            return;
        }

        company.setName( request.getName() );
        company.setTaxId( request.getTaxId() );
        company.setAddress( request.getAddress() );
    }
}
