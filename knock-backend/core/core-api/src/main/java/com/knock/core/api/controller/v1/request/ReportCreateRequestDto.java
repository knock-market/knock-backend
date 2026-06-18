package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReportCreateRequestDto(@NotNull ReportTargetType targetType, @NotNull @Positive Long targetId,
		@NotNull ReportReason reason, @Size(max = 1000) String description) {
}
