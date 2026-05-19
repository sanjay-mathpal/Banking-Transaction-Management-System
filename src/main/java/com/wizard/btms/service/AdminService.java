package com.wizard.btms.service;

import com.wizard.btms.dto.*;
import com.wizard.btms.entity.*;
import com.wizard.btms.repository.AccountRequestRepository;
import com.wizard.btms.repository.BankAccountRepository;
import com.wizard.btms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final BankAccountRepository bankAccountRepository;

    public List<UserResponse> getPendingUsers() {

        List<User> users =
                userRepository.findByStatus(
                        UserStatus.PENDING
                );

        return users.stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Transactional
    public void updateUserStatus(
            UpdateUserStatusRequest request
    ) {

        User user = userRepository.findById(
                        request.getUserId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));

        user.setStatus(request.getStatus());

        userRepository.save(user);
    }

    @Transactional
    public BankAccountResponse processAccountRequest(AccountApprovalRequest requestDto) {

        AccountRequest request = accountRequestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != AccountRequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        // =========================
        // REJECT FLOW
        // =========================
        if (requestDto.getStatus() == AccountRequestStatus.REJECTED) {
            request.setStatus(AccountRequestStatus.REJECTED);
            accountRequestRepository.save(request);
            return null; // or return a response message DTO later
        }

        // =========================
        // APPROVE FLOW
        // =========================
        if (requestDto.getStatus() == AccountRequestStatus.APPROVED) {

            BankAccount account = BankAccount.builder()
                    .accountNumber(accountNumberGenerator.generateAccountNumber())
                    .accountType(request.getAccountType())
                    .user(request.getUser())
                    .balance(request.getInitialDeposit())
                    .active(true)
                    .build();

            BankAccount saved = bankAccountRepository.save(account);

            request.setStatus(AccountRequestStatus.APPROVED);
            accountRequestRepository.save(request);

            return BankAccountResponse.builder()
                    .accountNumber(saved.getAccountNumber())
                    .accountType(saved.getAccountType())
                    .balance(saved.getBalance())
                    .active(saved.getActive())
                    .build();
        }

        throw new RuntimeException("Invalid status provided");
    }

    private UserResponse mapToUserResponse(
            User user
    ) {

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }

    @Transactional
    public void updateAccountStatus(
            String accountNumber,
            UpdateAccountStatusRequest request
    ) {

        BankAccount account =
                bankAccountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Account not found"
                                ));

        account.setActive(
                request.getActive()
        );

        bankAccountRepository.save(account);
    }
}