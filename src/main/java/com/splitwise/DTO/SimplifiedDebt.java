package com.splitwise.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedDebt {
  private String fromUser;
  private String toUser;
  private BigDecimal amount;
}
