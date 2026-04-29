package com.splitwise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_split")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExpensesSplit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount_owed",nullable = false,precision = 10,scale = 2)
    private BigDecimal amountOwned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id",nullable = false)
    private Expenses expenses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;


    private Boolean isSettled = false;
}
