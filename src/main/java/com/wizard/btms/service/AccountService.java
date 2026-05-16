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
import com.wizard.btms.dto.TransferRequest;
import com.wizard.btms.entity.Transaction;
import com.wizard.btms.entity.TransactionType;
import com.wizard.btms.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final TransactionRepository transactionRepository;

    public BankAccountResponse createAccount(CreateBankAccountRequest request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BankAccount bankAccount = BankAccount.builder().accountNumber(accountNumberGenerator.generateAccountNumber()).accountType(request.getAccountType()).user(user).build();

        BankAccount savedAccount = bankAccountRepository.save(bankAccount);

        return BankAccountResponse.builder().accountNumber(savedAccount.getAccountNumber()).accountType(savedAccount.getAccountType()).balance(savedAccount.getBalance()).active(savedAccount.getActive()).build();
    }

    @Transactional
    public void transferMoney(TransferRequest request, String email) {
        BankAccount fromAccount = bankAccountRepository.findByAccountNumber(request.getFromAccountNumber()).orElseThrow(() -> new RuntimeException("Sender account not found"));

        BankAccount toAccount = bankAccountRepository.findByAccountNumber(request.getToAccountNumber()).orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (!fromAccount.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You can only transfer from your own account");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {

            throw new RuntimeException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder().amount(request.getAmount()).transactionType(TransactionType.TRANSFER).description(request.getDescription()).fromAccount(fromAccount).toAccount(toAccount).build();

        transactionRepository.save(transaction);

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);
    }
}