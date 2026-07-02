package com.knock.core.domain.seller;

import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.seller.SellerAccessMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.knock.core.support.TestConstants.TEST_MEMBER_ID;
import static com.knock.core.support.TestConstants.TEST_MEMBER_ID_2;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SellerAccessPolicyTest {

	@InjectMocks
	private SellerAccessPolicy sellerAccessPolicy;

	@Mock
	private SellerAccessMemberRepository sellerAccessMemberRepository;

	@Test
	@DisplayName("접근 멤버가 아니면 판매자 리소스 접근을 차단한다")
	void validateAccessMember_forbidden() {
		given(sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
			.willReturn(false);

		assertThatThrownBy(() -> sellerAccessPolicy.validateAccessMember(TEST_MEMBER_ID_2, TEST_MEMBER_ID))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
	}

	@Test
	@DisplayName("판매자 본인은 멤버십 조회 없이 접근할 수 있다")
	void validateOwnerOrAccessMember_owner() {
		sellerAccessPolicy.validateOwnerOrAccessMember(TEST_MEMBER_ID, TEST_MEMBER_ID);

		verify(sellerAccessMemberRepository, never()).existsActiveBySellerIdAndMemberId(TEST_MEMBER_ID, TEST_MEMBER_ID);
	}

	@Test
	@DisplayName("판매자가 아니면 활성 접근 멤버십을 요구한다")
	void validateOwnerOrAccessMember_accessMember() {
		given(sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
			.willReturn(true);

		sellerAccessPolicy.validateOwnerOrAccessMember(TEST_MEMBER_ID_2, TEST_MEMBER_ID);

		verify(sellerAccessMemberRepository).existsActiveBySellerIdAndMemberId(TEST_MEMBER_ID, TEST_MEMBER_ID_2);
	}

}
