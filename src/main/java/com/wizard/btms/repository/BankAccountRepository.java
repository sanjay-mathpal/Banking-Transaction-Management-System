package com.wizard.btms.repository;

import com.wizard.btms.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM BankAccount b
            WHERE b.accountNumber = :accountNumber
            """)
    Optional<BankAccount> findByAccountNumberForUpdate(
            String accountNumber
    );

    List<BankAccount> findByUserEmail(String email);
}