package com.splitwise.DTO;

import com.splitwise.entity.Expenses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Expenses.SplitType splitType;
    private String paidBy;
    private LocalDateTime createdAt;
    private List<SplitDetail> splits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitDetail{
        private Long userId;
        private String userName;
        private BigDecimal amountOwed;
        private Boolean isSettled;
    }

}
