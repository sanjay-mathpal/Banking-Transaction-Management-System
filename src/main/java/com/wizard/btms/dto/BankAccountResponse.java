package com.wizard.btms.dto;

import com.wizard.btms.entity.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BankAccountResponse {

    private String accountNumber;

    private AccountType accountType;

    private BigDecimal balance;

    private Boolean active;
}