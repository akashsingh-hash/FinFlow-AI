package com.finflow.ai.module.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    private String taxId;
    private String address;
}
