package com.wizard.btms.controller;

import com.wizard.btms.dto.*;
import com.wizard.btms.service.AccountService;
import com.wizard.btms.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users/pending")
    public List<UserResponse> getPendingUsers() {

        return adminService.getPendingUsers();
    }

    @PatchMapping("/users/status")
    public String updateUserStatus(@Valid @RequestBody UpdateUserStatusRequest request)
    {
        adminService.updateUserStatus(request);
        return "User status updated successfully";
    }

    @PatchMapping("/account-requests/process")
    public BankAccountResponse process(@RequestBody AccountApprovalRequest request) {
        return adminService.processAccountRequest(request);
    }

    @Operation(
            summary = "Freeze or unfreeze bank account"
    )
    @PutMapping("/accounts/{accountNumber}/status")
    public String updateAccountStatus(

            @PathVariable
            String accountNumber,

            @RequestBody
            @Valid
            UpdateAccountStatusRequest request
    ) {

        adminService.updateAccountStatus(
                accountNumber,
                request
        );

        return request.getActive()
                ? "Account activated successfully"
                : "Account frozen successfully";
    }
}