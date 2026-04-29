package com.splitwise.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotBlank(message = "User id is required")
    private Long userId;
}
