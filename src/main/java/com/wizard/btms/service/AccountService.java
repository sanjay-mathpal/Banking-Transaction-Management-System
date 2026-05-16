package com.wizard.btms.service;

import com.wizard.btms.dto.BankAccountResponse;
import com.wizard.btms.dto.CreateBankAccountRequest;
import com.wizard.btms.dto.TransactionResponse;
import com.wizard.btms.entity.BankAccount;
import com.wizard.btms.entity.User;
import com.wizard.btms.exception.AccountNotFoundException;
import com.wizard.btms.exception.InsufficientBalanceException;
import com.wizard.btms.exception.UnauthorizedAccountAccessException;
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
import java.util.List;

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
            throw new UnauthorizedAccountAccessException(
                    "You can only transfer from your own account"
            );
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder().amount(request.getAmount()).transactionType(TransactionType.TRANSFER).description(request.getDescription()).fromAccount(fromAccount).toAccount(toAccount).build();

        transactionRepository.save(transaction);

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);
    }

    public List<BankAccountResponse> getMyAccounts(String email)
    {
        List<BankAccount> accounts = bankAccountRepository.findByUserEmail(email);

        return accounts.stream()
                .map(account -> BankAccountResponse.builder()
                        .accountNumber(account.getAccountNumber())
                        .accountType(account.getAccountType())
                        .balance(account.getBalance())
                        .active(account.getActive())
                        .build())
                .toList();
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber, String email)
    {
        BankAccount account =
                bankAccountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account not found"
                                ));

        if (!account.getUser().getEmail().equals(email)) {

            throw new UnauthorizedAccountAccessException(
                    "You can only view your own account transactions"
            );
        }

        List<Transaction> transactions =
                transactionRepository
                        .findTransactionsByAccountNumber(
                                accountNumber
                        );

        return transactions.stream()
                .map(transaction ->
                        TransactionResponse.builder()
                                .amount(transaction.getAmount())
                                .transactionType(
                                        transaction.getTransactionType()
                                )
                                .fromAccount(
                                        transaction.getFromAccount() != null
                                                ? transaction.getFromAccount()
                                                  .getAccountNumber()
                                                : null
                                )
                                .toAccount(
                                        transaction.getToAccount() != null
                                                ? transaction.getToAccount()
                                                  .getAccountNumber()
                                                : null
                                )
                                .description(
                                        transaction.getDescription()
                                )
                                .createdAt(
                                        transaction.getCreatedAt()
                                )
                                .build()
                )
                .toList();
    }
}