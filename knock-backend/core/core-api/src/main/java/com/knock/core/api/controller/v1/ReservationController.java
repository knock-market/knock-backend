package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ReservationCreateRequestDto;
import com.knock.core.api.controller.v1.response.ReservationCreateResponseDto;
import com.knock.core.api.controller.v1.response.ReservationResponseDto;
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
	public ApiResponse<ReservationCreateResponseDto> createReservation(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody ReservationCreateRequestDto request) {
		ReservationCreateData data = new ReservationCreateData(request.itemId(), principal.getMemberId());
		ReservationCreateResponseDto response = new ReservationCreateResponseDto(reservationService.createReservation(data));
		return ApiResponse.success(response);
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
	public ApiResponse<List<ReservationResponseDto>> getReservationsByItem(@PathVariable Long itemId) {
		List<ReservationResult> results = reservationService.getReservationsByItem(itemId);
		List<ReservationResponseDto> response = results.stream().map(ReservationResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	@GetMapping("/api/v1/reservations/my")
	public ApiResponse<List<ReservationResponseDto>> getMyReservations(
			@AuthenticationPrincipal MemberPrincipal principal) {
		List<ReservationResult> results = reservationService.getMyReservations(principal.getMemberId());
		List<ReservationResponseDto> response = results.stream().map(ReservationResponseDto::from).toList();
		return ApiResponse.success(response);
	}

}
