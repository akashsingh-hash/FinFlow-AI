package com.finflow.ai.module.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRequest {
    @NotBlank(message = "Vendor name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String address;
}
