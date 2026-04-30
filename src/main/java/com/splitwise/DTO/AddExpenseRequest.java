package com.splitwise.DTO;

import com.splitwise.entity.Expenses;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.aspectj.bridge.IMessage;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AddExpenseRequest {
    @NotNull(message = "Group ID is required")
    private Long groupId;
    @NotNull(message = "Description is required")
    private String description;
    @NotNull(message = "Amount should be added")
    @DecimalMin(value = "0.01",message = "Amount should be greater than ")
    private BigDecimal amount;
    @NotNull(message = "Split Type is required")
    private Expenses.SplitType splitType;

    List<SplitDetails> splits;

    @Data
    public static class SplitDetails{
        private Long userId;
        private BigDecimal value;
    }

}
