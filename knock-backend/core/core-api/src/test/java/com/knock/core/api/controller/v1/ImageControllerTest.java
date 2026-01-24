package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.image.ImageService;
import com.knock.infra.s3.dto.ImageUploadResult;
import com.knock.test.api.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.io.IOException;

import static com.knock.core.support.TestConstants.TEST_EMAIL;
import static com.knock.core.support.TestConstants.TEST_IMAGE_URL;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

class ImageControllerTest extends RestDocsTest {

	private ImageService imageService;

	private ImageController imageController;

	private MemberPrincipal principal;

	@BeforeEach
	public void setUp() {
		imageService = mock(ImageService.class);
		imageController = new ImageController(imageService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(imageController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("이미지 업로드 성공")
	void uploadImage_success() throws IOException {
		// given
		ImageUploadResult result = new ImageUploadResult("test.jpg", TEST_IMAGE_URL, "items/test.jpg");
		given(imageService.uploadImage(any())).willReturn(result);

		// RestAssuredMockMvc for multipart is slightly different
		// But we can use the MockMvc compatibility or just use given() if supported

		restDocGiven().multiPart("file", "test.jpg", "test content".getBytes(), "image/jpeg")
			.param("directory", "items")
			.post("/api/v1/images/upload")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/images/upload", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.originalFilename").type(JsonFieldType.STRING).description("원본 파일명"),
							fieldWithPath("data.imageUrl").type(JsonFieldType.STRING).description("이미지 URL"),
							fieldWithPath("data.s3Key").type(JsonFieldType.STRING).description("저장된 파일 경로"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("이미지 삭제 성공")
	void deleteImage_success() {
		// when & then
		restDocGiven().queryParam("imageUrl", TEST_IMAGE_URL)
			.delete("/api/v1/images")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/images/delete", requestPreprocessor(), responsePreprocessor(),
					queryParameters(parameterWithName("imageUrl").description("삭제할 이미지 URL")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
