package com.finflow.ai.module.approval.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    private String comments;
}
