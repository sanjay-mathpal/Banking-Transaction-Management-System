package com.wizard.btms.controller;

import com.wizard.btms.dto.BankAccountResponse;
import com.wizard.btms.dto.CreateBankAccountRequest;
import com.wizard.btms.dto.TransactionResponse;
import com.wizard.btms.dto.TransferRequest;
import com.wizard.btms.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String transferMoney(@Valid @RequestBody TransferRequest request, Authentication authentication)
    {
        String email = authentication.getName();

        accountService.transferMoney(request, email);

        return "Transfer successful";
    }

    @GetMapping("/my")
    public List<BankAccountResponse> getMyAccounts(Authentication authentication)
    {
        String email = authentication.getName();

        return accountService.getMyAccounts(email);
    }

    @GetMapping("/{accountNumber}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable String accountNumber, Authentication authentication)
    {
        String email = authentication.getName();

        return accountService.getTransactionHistory(
                accountNumber,
                email
        );
    }
}