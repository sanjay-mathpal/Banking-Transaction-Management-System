package com.wizard.btms.repository;

import com.wizard.btms.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.fromAccount.accountNumber = :accountNumber
               OR t.toAccount.accountNumber = :accountNumber
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findTransactionsByAccountNumber(
            String accountNumber,
            Pageable pageable
    );

    @Query("""
       SELECT COALESCE(SUM(t.amount), 0)
       FROM Transaction t
       WHERE t.fromAccount.accountNumber = :accountNumber
       AND t.status = 'SUCCESS'
       AND t.createdAt BETWEEN :startOfDay AND :endOfDay
       """)
    BigDecimal getTodayTransferAmount(
            @Param("accountNumber")
            String accountNumber,

            @Param("startOfDay")
            LocalDateTime startOfDay,

            @Param("endOfDay")
            LocalDateTime endOfDay
    );
}