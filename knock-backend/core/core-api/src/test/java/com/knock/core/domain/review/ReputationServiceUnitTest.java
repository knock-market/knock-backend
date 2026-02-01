package com.knock.core.domain.review;

import com.knock.storage.db.core.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReputationServiceUnitTest {

	private final ReputationService reputationService = new ReputationService();

	@Test
	@DisplayName("일반 케이스: 5점 리뷰를 받으면 점수가 상승한다")
	void changeReputation_Normal() {
		// given
		Member member = createMember(36.5);
		int reviewScore = 5; // 가정: 5점이면 +1.0점 상승

		// when
		reputationService.changeReputation(member, reviewScore);

		// then
		// 36.5 + 1.0 = 37.5
		assertThat(member.getMannerTemperature()).isEqualTo(37.5);
	}

	@Test
	@DisplayName("최대값 제한: 점수가 올라도 100점을 넘을 수 없다")
	void changeReputation_Max_Limit() {
		// given
		Member member = createMember(99.5); // 100점 직전
		int reviewScore = 5; // +1.0점 상승 시도 (계산상 100.5)

		// when
		reputationService.changeReputation(member, reviewScore);

		// then
		// 100.0 에서 멈춰야 함 (Clamping 로직 검증)
		assertThat(member.getMannerTemperature()).isEqualTo(100.0);
	}

	@Test
	@DisplayName("최소값 제한: 점수가 떨어져도 0점 밑으로 갈 수 없다")
	void changeReputation_Min_Limit() {
		// given
		Member member = createMember(0.5); // 0점 직전
		int reviewScore = 1; // 가정: 1점이면 -5.0점 하락 시도 (계산상 -4.5)

		// when
		reputationService.changeReputation(member, reviewScore);

		// then
		// 0.0 에서 멈춰야 함
		assertThat(member.getMannerTemperature()).isEqualTo(0.0);
	}

	private Member createMember(double initialScore) {
		Member member = Member.builder()
			.email("test@example.com")
			.name("tester")
			.password("password")
			.nickname("testNick")
			.provider("kakao")
			.build();

		member.updateMannerTemperature(initialScore);

		return member;
	}

}