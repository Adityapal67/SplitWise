package com.splitwise.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime created_at;
    List<MemberResponse> members;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberResponse {
        private Long userId;
        private String name;
        private String email;
        private LocalDateTime joined_at;
    }
}
