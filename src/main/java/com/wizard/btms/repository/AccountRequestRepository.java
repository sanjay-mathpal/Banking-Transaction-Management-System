package com.wizard.btms.repository;

import com.wizard.btms.entity.AccountRequest;
import com.wizard.btms.entity.AccountRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {

    List<AccountRequest> findByStatus(AccountRequestStatus status);
}