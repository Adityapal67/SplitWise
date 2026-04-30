package com.splitwise.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private BigDecimal totalYouOwe;
    private BigDecimal totalOwedToYou;
    private List<BalanceDetail> balanceDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceDetail{
        private String withUser;
        private BigDecimal amount;
    }
}
