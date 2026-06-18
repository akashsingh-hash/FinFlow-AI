package com.finflow.ai.module.approval.dto;

import com.finflow.ai.module.approval.ApprovalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {
    private Long id;
    private Long expenseId;
    private String expenseTitle;
    private BigDecimal expenseAmount;
    private String expenseUserFullName;
    private Long approverId;
    private String approverFullName;
    private ApprovalStatus status;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
