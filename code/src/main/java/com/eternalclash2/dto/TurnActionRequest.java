package com.eternalclash2.dto;

import com.eternalclash2.domain.enums.ActionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TurnActionRequest(@NotNull(message = "actionType is required") ActionType actionType, @NotNull(message = "cityId is required")
                                Long cityId, Long targetCityId, @Min(value = 1, message = "soldierCount must be at least 1")
                                Integer soldierCount) {
}
