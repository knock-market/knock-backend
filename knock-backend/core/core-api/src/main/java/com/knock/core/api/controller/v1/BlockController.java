package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.response.BlockResponseDto;
import com.knock.core.domain.block.BlockService;
import com.knock.core.domain.block.dto.BlockResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BlockController {

	private final BlockService blockService;

	@PostMapping("/api/v1/blocks/{memberId}")
	public ApiResponse<BlockResponseDto> blockMember(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long memberId) {
		BlockResult result = blockService.blockMember(principal.getMemberId(), memberId);
		return ApiResponse.success(BlockResponseDto.from(result));
	}

	@DeleteMapping("/api/v1/blocks/{memberId}")
	public ApiResponse<Void> unblockMember(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long memberId) {
		blockService.unblockMember(principal.getMemberId(), memberId);
		return ApiResponse.success(null);
	}

	@GetMapping("/api/v1/blocks/my")
	public ApiResponse<List<BlockResponseDto>> getMyBlocks(@AuthenticationPrincipal MemberPrincipal principal) {
		List<BlockResponseDto> response = blockService.getMyBlocks(principal.getMemberId())
			.stream()
			.map(BlockResponseDto::from)
			.toList();
		return ApiResponse.success(response);
	}

}
