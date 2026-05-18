package com.wizard.btms.dto;

import com.wizard.btms.entity.AccountRequestStatus;
import lombok.Data;

@Data
public class AccountApprovalRequest {

    private Long requestId;

    private AccountRequestStatus status;
}