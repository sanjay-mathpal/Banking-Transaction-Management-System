package com.wizard.btms.controller;

import com.wizard.btms.dto.BankAccountResponse;
import com.wizard.btms.dto.CreateBankAccountRequest;
import com.wizard.btms.dto.TransferRequest;
import com.wizard.btms.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public BankAccountResponse createAccount(@Valid @RequestBody CreateBankAccountRequest request, Authentication authentication
    ) {
        String email = authentication.getName();
        return accountService.createAccount(request, email);
    }

    @PostMapping("/transfer")
    public String transferMoney(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        accountService.transferMoney(request, email);

        return "Transfer successful";
    }
}