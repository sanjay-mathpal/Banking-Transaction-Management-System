package com.wizard.btms.service;

import com.wizard.btms.dto.BankAccountResponse;
import com.wizard.btms.dto.CreateBankAccountRequest;
import com.wizard.btms.dto.TransactionResponse;
import com.wizard.btms.entity.*;
import com.wizard.btms.exception.AccountNotFoundException;
import com.wizard.btms.exception.FrozenAccountException;
import com.wizard.btms.exception.InsufficientBalanceException;
import com.wizard.btms.exception.UnauthorizedAccountAccessException;
import com.wizard.btms.repository.AccountRequestRepository;
import com.wizard.btms.repository.BankAccountRepository;
import com.wizard.btms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.wizard.btms.dto.TransferRequest;
import com.wizard.btms.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final TransactionAuditService transactionAuditService;

    public String createAccount(CreateBankAccountRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        AccountRequest accountRequest = AccountRequest.builder()
                .user(user)
                .accountType(request.getAccountType())
                .initialDeposit(BigDecimal.ZERO)
                .status(AccountRequestStatus.PENDING)
                .build();

        accountRequestRepository.save(accountRequest);

        return "Account request submitted successfully. Awaiting admin approval.";
    }

    @Transactional
    public void transferMoney(
            TransferRequest request,
            String email
    ) {

        BankAccount fromAccount =
                bankAccountRepository
                        .findByAccountNumberForUpdate(
                                request.getFromAccountNumber()
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Sender account not found"
                                ));

        BankAccount toAccount =
                bankAccountRepository
                        .findByAccountNumberForUpdate(
                                request.getToAccountNumber()
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Receiver account not found"
                                ));

        if (!fromAccount.getUser().getEmail().equals(email)) {

            throw new UnauthorizedAccountAccessException(
                    "You can only transfer from your own account"
            );
        }

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();

        transactionAuditService.saveTransaction(transaction);

        try {

            if (!fromAccount.getActive()) {

                transaction.setStatus(
                        TransactionStatus.FAILED
                );

                transaction.setFailureReason(
                        "SENDER_ACCOUNT_FROZEN"
                );

                transactionAuditService.saveTransaction(transaction);

                throw new FrozenAccountException(
                        "Sender account is frozen"
                );
            }

            if (!toAccount.getActive()) {

                transaction.setStatus(
                        TransactionStatus.FAILED
                );

                transaction.setFailureReason(
                        "RECEIVER_ACCOUNT_FROZEN"
                );

                transactionAuditService.saveTransaction(transaction);

                throw new FrozenAccountException(
                        "Receiver account is frozen"
                );
            }

            if (fromAccount.getBalance()
                    .compareTo(request.getAmount()) < 0) {

                transaction.setStatus(
                        TransactionStatus.FAILED
                );

                transaction.setFailureReason(
                        "INSUFFICIENT_BALANCE"
                );

                transactionAuditService.saveTransaction(transaction);

                throw new InsufficientBalanceException(
                        "Insufficient balance"
                );
            }

            fromAccount.setBalance(
                    fromAccount.getBalance()
                            .subtract(request.getAmount())
            );

            toAccount.setBalance(
                    toAccount.getBalance()
                            .add(request.getAmount())
            );

            bankAccountRepository.save(fromAccount);

            bankAccountRepository.save(toAccount);

            transaction.setStatus(
                    TransactionStatus.SUCCESS
            );

            transactionAuditService.saveTransaction(transaction);

        } catch (Exception ex) {

            if (transaction.getStatus()
                    != TransactionStatus.FAILED) {

                transaction.setStatus(
                        TransactionStatus.FAILED
                );

                transaction.setFailureReason(
                        "TRANSFER_FAILED"
                );

                transactionAuditService.saveTransaction(transaction);
            }

            throw ex;
        }
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

    public Page<TransactionResponse> getTransactionHistory(
            String accountNumber,
            String email,
            int page,
            int size
    ) {

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

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Transaction> transactions =
                transactionRepository
                        .findTransactionsByAccountNumber(
                                accountNumber,
                                pageable
                        );

        return transactions.map(transaction ->
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
        );
    }
}