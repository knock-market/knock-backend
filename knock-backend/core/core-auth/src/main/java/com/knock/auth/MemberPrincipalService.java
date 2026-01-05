package com.knock.auth;

/**
 * 세션 기반 인증에서는 UserDetailsService가 필요하지 않습니다. MemberPrincipal은 로그인 시 SecurityContext에
 * 저장되고, Redis 세션에서 직접 복원됩니다.
 *
 * 이 클래스는 폼 로그인이나 Remember-Me 기능이 필요할 경우에만 사용하세요.
 */
// @Service - 세션 기반 인증에서는 Bean으로 등록하지 않음
public class MemberPrincipalService {

	// 필요 시 구현

}
