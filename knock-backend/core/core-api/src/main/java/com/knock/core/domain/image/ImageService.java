package com.knock.core.domain.image;

import com.knock.infra.s3.dto.ImageUploadResult;
import com.knock.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final S3Service s3Service;

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	public ImageUploadResult uploadImage(MultipartFile file, String directory) {
		validateImageFile(file);
		return s3Service.uploadImage(file, directory);
	}

	public void deleteImage(String imageUrl) {
		s3Service.deleteImage(imageUrl);
	}

	private void validateImageFile(MultipartFile file) {
		// todo : validator 추가
		if (file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어있습니다.");
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new IllegalArgumentException("확장자가 없는 파일입니다.");
		}

		String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp 만 가능)");
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
		}
	}

}
