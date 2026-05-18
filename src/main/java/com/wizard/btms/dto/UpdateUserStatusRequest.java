package com.wizard.btms.dto;

import com.wizard.btms.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Status is required")
    private UserStatus status;
}