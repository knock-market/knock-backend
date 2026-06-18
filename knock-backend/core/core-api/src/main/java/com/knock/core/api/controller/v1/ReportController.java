package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ReportCreateRequestDto;
import com.knock.core.api.controller.v1.response.ReportResponseDto;
import com.knock.core.domain.report.ReportService;
import com.knock.core.domain.report.dto.ReportCreateData;
import com.knock.core.domain.report.dto.ReportResult;
import com.knock.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@PostMapping("/api/v1/reports")
	public ApiResponse<ReportResponseDto> createReport(@AuthenticationPrincipal MemberPrincipal principal,
			@Valid @RequestBody ReportCreateRequestDto request) {
		ReportCreateData data = new ReportCreateData(request.targetType(), request.targetId(), request.reason(),
				request.description());
		ReportResult result = reportService.createReport(principal.getMemberId(), data);
		return ApiResponse.success(ReportResponseDto.from(result));
	}

}
