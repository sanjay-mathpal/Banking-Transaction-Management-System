package com.wizard.btms.service;

import com.wizard.btms.dto.BankAccountResponse;
import com.wizard.btms.dto.CreateBankAccountRequest;
import com.wizard.btms.entity.BankAccount;
import com.wizard.btms.entity.User;
import com.wizard.btms.repository.BankAccountRepository;
import com.wizard.btms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    public BankAccountResponse createAccount(
            CreateBankAccountRequest request,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        BankAccount bankAccount = BankAccount.builder()
                .accountNumber(
                        accountNumberGenerator.generateAccountNumber()
                )
                .accountType(request.getAccountType())
                .user(user)
                .build();

        BankAccount savedAccount =
                bankAccountRepository.save(bankAccount);

        return BankAccountResponse.builder()
                .accountNumber(savedAccount.getAccountNumber())
                .accountType(savedAccount.getAccountType())
                .balance(savedAccount.getBalance())
                .active(savedAccount.getActive())
                .build();
    }
}