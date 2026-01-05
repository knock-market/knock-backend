package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.GroupCreateRequestDto;
import com.knock.core.api.controller.v1.request.GroupJoinRequestDto;
import com.knock.core.api.controller.v1.request.InviteCodeRequestDto;
import com.knock.core.api.controller.v1.response.GroupResponseDto;
import com.knock.core.api.controller.v1.response.InviteCodeResponseDto;
import com.knock.core.domain.group.GroupService;
import com.knock.core.domain.group.dto.GroupData;
import com.knock.core.domain.group.dto.GroupResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {

	private final GroupService groupService;

	@PostMapping("/api/v1/groups")
	public ApiResponse<GroupIdResponseDto> createGroup(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody GroupCreateRequestDto request) {
		Long groupId = groupService.createGroup(principal.getMemberId(),
				new GroupData.Create(request.name(), request.description()));
		GroupIdResponseDto response = new GroupIdResponseDto(groupId);
		return ApiResponse.success(response);
	}

	@PostMapping("/api/v1/groups/{groupId}/invite-codes")
	public ApiResponse<InviteCodeResponseDto> generateInviteCode(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long groupId, @RequestBody InviteCodeRequestDto request) {
		return ApiResponse
			.success(groupService.generateTimedInviteCode(principal.getMemberId(), groupId, request.duration()));
	}

	@PostMapping("/api/v1/groups/join")
	public ApiResponse<Long> joinGroup(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody GroupJoinRequestDto request) {
		return ApiResponse.success(groupService.joinGroup(principal.getMemberId(), GroupData.Join.of(request)));
	}

	@PostMapping("/api/v1/groups/{groupId}/leave")
	public ApiResponse<?> leaveGroup(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long groupId) {
		groupService.leaveGroup(principal.getMemberId(), groupId);
		return ApiResponse.success();
	}

	@GetMapping("/api/v1/groups/my")
	public ApiResponse<List<GroupResponseDto>> getMyGroups(@AuthenticationPrincipal MemberPrincipal principal) {
		List<GroupResponseDto> groups = groupService.getMyGroups(principal.getMemberId())
			.stream()
			.map(GroupResponseDto::from)
			.toList();
		return ApiResponse.success(groups);
	}

	@GetMapping("/api/v1/groups/{groupId}")
	public ApiResponse<GroupResponseDto> getGroupDetail(@AuthenticationPrincipal MemberPrincipal principal) {
		GroupResult result = groupService.getGroupDetail(principal.getMemberId());
		GroupResponseDto response = GroupResponseDto.from(result);
		return ApiResponse.success(response);
	}

	public record GroupIdResponseDto(Long id) {
	}

}
