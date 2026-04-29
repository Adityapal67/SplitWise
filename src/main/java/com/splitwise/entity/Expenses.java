package com.splitwise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "expenses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Expenses {
    public enum SplitType {EQUAL,EXACT,PERCENTAGE}
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id",nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by",nullable = false)
    private User user;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    @JoinColumn()
    private SplitType split_type;

    @OneToMany(mappedBy = "expenses", cascade = CascadeType.ALL)
    private List<ExpensesSplit> splits;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
