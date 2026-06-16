package com.knock.infra.s3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

	private static final String TEST_BUCKET = "test-bucket";

	private S3ServiceImpl s3Service;

	@Mock
	private S3Client s3Client;

	@BeforeEach
	void setUp() {
		s3Service = new S3ServiceImpl(s3Client);
		ReflectionTestUtils.setField(s3Service, "bucketName", TEST_BUCKET);
	}

	@Test
	@DisplayName("이미지 삭제 성공 - URL path에서 S3 key를 추출한다")
	void deleteImage_success_extractsKeyFromUrlPath() {
		// given
		String imageUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/items/photo.jpg";

		// when
		s3Service.deleteImage(imageUrl);

		// then
		DeleteObjectRequest request = captureDeleteRequest();
		assertThat(request.bucket()).isEqualTo(TEST_BUCKET);
		assertThat(request.key()).isEqualTo("items/photo.jpg");
	}

	@Test
	@DisplayName("이미지 삭제 성공 - query string은 S3 key에 포함하지 않는다")
	void deleteImage_success_ignoresQueryString() {
		// given
		String imageUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/items/photo.jpg?versionId=test";

		// when
		s3Service.deleteImage(imageUrl);

		// then
		DeleteObjectRequest request = captureDeleteRequest();
		assertThat(request.key()).isEqualTo("items/photo.jpg");
	}

	@Test
	@DisplayName("이미지 삭제 실패 - 유효하지 않은 URL은 S3 삭제를 호출하지 않는다")
	void deleteImage_fail_invalidUrl() {
		Stream.of(null, "", " ", "not a url", "items/photo.jpg", "https://test-bucket.s3.ap-northeast-2.amazonaws.com")
			.forEach((imageUrl) -> {
				assertThatThrownBy(() -> s3Service.deleteImage(imageUrl)).isInstanceOf(ResponseStatusException.class)
					.hasMessageContaining("400 BAD_REQUEST")
					.hasMessageContaining("Invalid image URL");

				verifyNoInteractions(s3Client);
			});
	}

	private DeleteObjectRequest captureDeleteRequest() {
		ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
		verify(s3Client).deleteObject(captor.capture());
		return captor.getValue();
	}

}
