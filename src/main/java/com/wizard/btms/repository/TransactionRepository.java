package com.wizard.btms.repository;

import com.wizard.btms.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
       SELECT t
       FROM Transaction t
       WHERE t.fromAccount.accountNumber = :accountNumber
          OR t.toAccount.accountNumber = :accountNumber
       ORDER BY t.createdAt DESC
       """)
    List<Transaction> findTransactionsByAccountNumber(
            @Param("accountNumber") String accountNumber
    );
}