package com.knock.core.domain.member.event;

public record MemberCreatedEvent(Long memberId, String memberName) {
}
