package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.reservation.dto.ReservationResult;
import com.knock.core.enums.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponseDto(
        Long id,
        Long itemId,
        String itemTitle,
        Long memberId,
        String memberName,
        ReservationStatus status,
        LocalDateTime createdAt) {

    public static ReservationResponseDto from(ReservationResult result) {
        return new ReservationResponseDto(
                result.id(),
                result.itemId(),
                result.itemTitle(),
                result.memberId(),
                result.memberName(),
                result.status(),
                result.createdAt());
    }
}
