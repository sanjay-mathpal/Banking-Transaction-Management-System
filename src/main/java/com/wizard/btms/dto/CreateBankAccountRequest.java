package com.wizard.btms.dto;

import com.wizard.btms.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBankAccountRequest {

    @NotNull
    private AccountType accountType;
}