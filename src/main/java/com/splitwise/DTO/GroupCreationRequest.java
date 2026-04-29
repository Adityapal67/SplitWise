package com.splitwise.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupCreationRequest {
    @NotBlank(message = "Group name is required")
    private String groupName;
    private String description;
}
