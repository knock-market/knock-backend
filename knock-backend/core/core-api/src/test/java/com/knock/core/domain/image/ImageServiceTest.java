package com.knock.core.domain.image;

import com.knock.core.domain.image.dto.ImageDeleteData;
import com.knock.core.domain.image.dto.ImageUploadData;
import com.knock.infra.s3.dto.ImageUploadResult;
import com.knock.infra.s3.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static com.knock.core.support.TestConstants.TEST_IMAGE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@InjectMocks
	private ImageService imageService;

	@Mock
	private S3Service s3Service;

	private static final String TEST_DIRECTORY = "items";

	@Nested
	@DisplayName("이미지 업로드")
	class UploadImage {

		@Test
		@DisplayName("성공 - JPG")
		void success_jpg() {
			// given
			MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg",
					"test image content".getBytes());
			ImageUploadData data = new ImageUploadData(file, TEST_DIRECTORY);
			ImageUploadResult expected = new ImageUploadResult("test.jpg", TEST_IMAGE_URL, "items/test.jpg");

			given(s3Service.uploadImage(any(MultipartFile.class), anyString())).willReturn(expected);

			// when
			ImageUploadResult result = imageService.uploadImage(data);

			// then
			assertThat(result.imageUrl()).isEqualTo(TEST_IMAGE_URL);
			verify(s3Service).uploadImage(file, TEST_DIRECTORY);
		}

		@Test
		@DisplayName("성공 - PNG")
		void success_png() {
			// given
			MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png",
					"test image content".getBytes());
			ImageUploadData data = new ImageUploadData(file, TEST_DIRECTORY);
			ImageUploadResult expected = new ImageUploadResult("test.png", TEST_IMAGE_URL, "items/test.png");

			given(s3Service.uploadImage(any(MultipartFile.class), anyString())).willReturn(expected);

			// when
			ImageUploadResult result = imageService.uploadImage(data);

			// then
			assertThat(result.originalFilename()).isEqualTo("test.png");
		}

		@Test
		@DisplayName("실패 - 빈 파일")
		void fail_emptyFile() {
			// given
			MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
			ImageUploadData data = new ImageUploadData(file, TEST_DIRECTORY);

			// when & then
			assertThatThrownBy(() -> imageService.uploadImage(data)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("파일이 비어있습니다");
		}

		@Test
		@DisplayName("실패 - 파일 크기 초과")
		void fail_fileTooLarge() {
			// given
			byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
			MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", largeContent);
			ImageUploadData data = new ImageUploadData(file, TEST_DIRECTORY);

			// when & then
			assertThatThrownBy(() -> imageService.uploadImage(data)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("10MB를 초과");
		}

		@Test
		@DisplayName("실패 - 지원하지 않는 확장자")
		void fail_unsupportedExtension() {
			// given
			MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
			ImageUploadData data = new ImageUploadData(file, TEST_DIRECTORY);

			// when & then
			assertThatThrownBy(() -> imageService.uploadImage(data)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("지원하지 않는 이미지 형식");
		}

		@Test
		@DisplayName("실패 - 이미지가 아닌 컨텐츠 타입")
		void fail_notImageContentType() {
			// given
			MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "application/octet-stream",
					"test content".getBytes());
			ImageUploadData data = new ImageUploadData(file, TEST_DIRECTORY);

			// when & then
			assertThatThrownBy(() -> imageService.uploadImage(data)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("이미지 파일만 업로드");
		}

	}

	@Nested
	@DisplayName("이미지 삭제")
	class DeleteImage {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			ImageDeleteData data = new ImageDeleteData(TEST_IMAGE_URL);

			// when
			imageService.deleteImage(data);

			// then
			verify(s3Service).deleteImage(TEST_IMAGE_URL);
		}

	}

}
