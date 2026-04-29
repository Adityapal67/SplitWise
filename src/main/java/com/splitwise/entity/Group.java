package com.splitwise.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_group")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = " created_at")
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> members;

    @PrePersist
    protected void onCreate(){
        this.created_at = LocalDateTime.now();
    }
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by",nullable = false)
    private User createdBy;


}
