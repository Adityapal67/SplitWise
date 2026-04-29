package com.splitwise.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id",nullable = false)
    private Group group;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by",nullable = false)
    private User paidBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_to",nullable = false)
    private User paidTo;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal amount;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @PrePersist
    protected void onCreate() {
        this.settledAt = LocalDateTime.now();
    }
}
