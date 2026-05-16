package com.wizard.btms.dto;

import com.wizard.btms.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private BigDecimal amount;

    private TransactionType transactionType;

    private String fromAccount;

    private String toAccount;

    private String description;

    private LocalDateTime createdAt;
}