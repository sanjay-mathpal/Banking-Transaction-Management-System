package com.wizard.btms.service;

import com.wizard.btms.entity.Transaction;
import com.wizard.btms.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionAuditService {

    private final TransactionRepository transactionRepository;

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveTransaction(
            Transaction transaction
    ) {

        transactionRepository.save(transaction);
    }
}