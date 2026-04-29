package com.splitwise.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_member",uniqueConstraints = @UniqueConstraint(columnNames = {"group_id","user_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id" , nullable = false)
    private Group group;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;


    @PrePersist
    protected void onCreate(){
        this.joinedAt = LocalDateTime.now();
    }

}
