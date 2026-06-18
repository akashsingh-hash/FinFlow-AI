package com.finflow.ai.module.analytics;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorSpend {
    private String vendorName;
    private BigDecimal amount;
}
