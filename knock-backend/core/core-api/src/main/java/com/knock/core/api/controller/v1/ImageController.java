package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.domain.image.ImageService;
import com.knock.core.support.response.ApiResponse;
import com.knock.infra.s3.dto.ImageUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@PostMapping("/api/v1/images/upload")
	public ApiResponse<ImageUploadResult> uploadImage(@AuthenticationPrincipal MemberPrincipal principal, @RequestParam("file") MultipartFile file, @RequestParam("directory") String directory) {
		return ApiResponse.success(imageService.uploadImage(file, directory));
	}

	@DeleteMapping("/api/v1/images")
	public ApiResponse<Void> deleteImage(@AuthenticationPrincipal MemberPrincipal principal, @RequestParam("imageUrl") String imageUrl) {
		imageService.deleteImage(imageUrl);
		return ApiResponse.success(null);
	}

}
