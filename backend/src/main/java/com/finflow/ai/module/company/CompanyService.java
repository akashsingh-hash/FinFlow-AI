package com.finflow.ai.module.company;

import com.finflow.ai.module.company.dto.CompanyRequest;
import com.finflow.ai.module.company.dto.CompanyResponse;

public interface CompanyService {
    CompanyResponse createCompany(CompanyRequest request);
    CompanyResponse updateCompany(Long id, CompanyRequest request);
    CompanyResponse getCompanyById(Long id);
}
