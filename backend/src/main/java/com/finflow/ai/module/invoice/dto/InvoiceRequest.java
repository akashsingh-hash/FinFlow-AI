package com.finflow.ai.module.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Invoice amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
}
