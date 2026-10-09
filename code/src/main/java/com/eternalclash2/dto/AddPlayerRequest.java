package com.eternalclash2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddPlayerRequest(
        @NotBlank(message = "Player name is required") 
        @Size(max = 50, message = "Player name must not exceed 50 characters") 
        String name) {
}
