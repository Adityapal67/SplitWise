package com.splitwise.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettleRequest {
    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Paid to user ID is required")
    private Long paidToUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

}
