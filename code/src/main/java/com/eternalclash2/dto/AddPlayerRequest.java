package com.eternalclash2.dto;

import jakarta.validation.constraints.NotBlank;

public record AddPlayerRequest(@NotBlank(message = "Player name is required") String name) {
}
