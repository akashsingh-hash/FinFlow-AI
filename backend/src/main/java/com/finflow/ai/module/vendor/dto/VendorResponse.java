package com.finflow.ai.module.vendor.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Long companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
