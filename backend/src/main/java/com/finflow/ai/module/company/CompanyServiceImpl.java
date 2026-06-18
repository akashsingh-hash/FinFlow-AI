package com.finflow.ai.module.company;

import com.finflow.ai.exception.ResourceNotFoundException;
import com.finflow.ai.module.auditlog.AuditLogService;
import com.finflow.ai.module.company.dto.CompanyRequest;
import com.finflow.ai.module.company.dto.CompanyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        Company company = companyMapper.toEntity(request);
        Company saved = companyRepository.save(company);
        
        auditLogService.log("CREATE_COMPANY", "Company", saved.getId(), null, saved);
        
        return companyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        
        Company oldCompanyCopy = Company.builder()
                .id(company.getId())
                .name(company.getName())
                .taxId(company.getTaxId())
                .address(company.getAddress())
                .build();
                
        companyMapper.updateEntityFromRequest(request, company);
        Company updated = companyRepository.save(company);
        
        auditLogService.log("UPDATE_COMPANY", "Company", updated.getId(), oldCompanyCopy, updated);
        
        return companyMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        return companyMapper.toResponse(company);
    }
}
