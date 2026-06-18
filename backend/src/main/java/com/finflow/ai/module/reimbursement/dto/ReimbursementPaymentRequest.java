package com.finflow.ai.module.reimbursement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementPaymentRequest {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotBlank(message = "Payment reference is required")
    private String paymentReference;
}
