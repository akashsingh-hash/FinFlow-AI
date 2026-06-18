package com.finflow.ai.module.invoice.dto;

import com.finflow.ai.module.invoice.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private String fileUrl;
    private Long vendorId;
    private String vendorName;
    private Long companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
