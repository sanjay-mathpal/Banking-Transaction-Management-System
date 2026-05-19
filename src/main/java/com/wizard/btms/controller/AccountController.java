package com.wizard.btms.controller;

import com.wizard.btms.dto.BankAccountResponse;
import com.wizard.btms.dto.CreateBankAccountRequest;
import com.wizard.btms.dto.TransactionResponse;
import com.wizard.btms.dto.TransferRequest;
import com.wizard.btms.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account APIs", description = "Bank account management APIs")
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "Create bank account",
            description = "Creates a new bank account for authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public String createAccount(@Valid @RequestBody CreateBankAccountRequest request, Authentication authentication
    ) {
        String email = authentication.getName();
        return accountService.createAccount(request, email);
    }

    @Operation(
            summary = "Transfer money",
            description = "Transfers money between bank accounts securely"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/transfer")
    public String transferMoney(@Valid @RequestBody TransferRequest request, Authentication authentication)
    {
        String email = authentication.getName();

        accountService.transferMoney(request, email);

        return "Transfer successful";
    }

    @Operation(
            summary = "Get user accounts",
            description = "Returns all bank accounts of authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my")
    public List<BankAccountResponse> getMyAccounts(Authentication authentication)
    {
        String email = authentication.getName();

        return accountService.getMyAccounts(email);
    }

    @Operation(
            summary = "Get paginated transaction history",
            description = "Fetch paginated transactions for a bank account"
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions fetched successfully"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized access",
                    content = @Content
            )
    })
    @GetMapping("/{accountNumber}/transactions")
    public Page<TransactionResponse> getTransactionHistory(
            @PathVariable String accountNumber,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            Authentication authentication
    ) {

        return accountService.getTransactionHistory(
                accountNumber,
                authentication.getName(),
                page,
                size
        );
    }
}