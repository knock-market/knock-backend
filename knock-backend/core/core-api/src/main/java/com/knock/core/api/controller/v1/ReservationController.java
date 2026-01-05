package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ReservationCreateRequestDto;
import com.knock.core.domain.reservation.ReservationService;
import com.knock.core.domain.reservation.dto.ReservationCreateData;
import com.knock.core.domain.reservation.dto.ReservationResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@PostMapping("/api/v1/reservations")
	public ApiResponse<Long> createReservation(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody ReservationCreateRequestDto request) {
		ReservationCreateData data = new ReservationCreateData(request.itemId(), principal.getMemberId());
		Long reservationId = reservationService.createReservation(data);
		return ApiResponse.success(reservationId);
	}

	@PatchMapping("/api/v1/reservations/{id}/approve")
	public ApiResponse<Void> approveReservation(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long id) {
		reservationService.approveReservation(principal.getMemberId(), id);
		return ApiResponse.success(null);
	}

	@PatchMapping("/api/v1/reservations/{id}/complete")
	public ApiResponse<?> completeReservation(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long id) {
		reservationService.completeReservation(principal.getMemberId(), id);
		return ApiResponse.success();
	}

	@PatchMapping("/api/v1/reservations/{id}/cancel")
	public ApiResponse<Void> cancelReservation(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long id) {
		reservationService.cancelReservation(principal.getMemberId(), id);
		return ApiResponse.success(null);
	}

	@GetMapping("/api/v1/items/{itemId}/reservations")
	public ApiResponse<List<ReservationResult>> getReservationsByItem(@PathVariable Long itemId) {
		return ApiResponse.success(reservationService.getReservationsByItem(itemId));
	}

	@GetMapping("/api/v1/reservations/my")
	public ApiResponse<List<ReservationResult>> getMyReservations(@AuthenticationPrincipal MemberPrincipal principal) {
		return ApiResponse.success(reservationService.getMyReservations(principal.getMemberId()));
	}

}
