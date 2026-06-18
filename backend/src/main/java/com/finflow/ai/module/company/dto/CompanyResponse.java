package com.finflow.ai.module.company.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String taxId;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
