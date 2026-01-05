package com.knock.infra.s3.service;

import com.knock.infra.s3.dto.ImageUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

	private final S3Client s3Client;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Override
	public ImageUploadResult uploadImage(MultipartFile file, String directory) {
		String originalFilename = file.getOriginalFilename();
		String extension = getExtension(originalFilename);
		String s3Key = directory + "/" + UUID.randomUUID() + extension;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.contentType(file.getContentType())
				.build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			String imageUrl = s3Client.utilities()
				.getUrl(GetUrlRequest.builder().bucket(bucketName).key(s3Key).build())
				.toExternalForm();

			log.info("Image uploaded successfully: {}", imageUrl);
			return new ImageUploadResult(originalFilename, imageUrl, s3Key);

		}
		catch (IOException e) {
			throw new RuntimeException("Failed to upload image to S3", e);
		}
	}

	@Override
	public void deleteImage(String imageUrl) {
		// URL에서 Key 추출 (간단한 구현, 실제로는 URL 파싱 필요)
		String key = extractKeyFromUrl(imageUrl);

		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

		s3Client.deleteObject(deleteObjectRequest);
		log.info("Image deleted from S3: {}", key);
	}

	private String getExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf("."));
	}

	// 이 부분은 실제 URL 구조에 따라 달라질 수 있음.
	// 예: https://bucket.s3.region.amazonaws.com/dir/file.jpg -> dir/file.jpg
	private String extractKeyFromUrl(String imageUrl) {
		// 임시 구현: 단순히 파일명만 추출하는 것이 아니라 전체 경로가 필요함.
		// 여기서는 MVP 구현을 위해 일단 URL이 S3 Presigned URL이 아니라고 가정
		try {
			java.net.URL url = java.net.URI.create(imageUrl).toURL();
			return url.getPath().substring(1); // leading slash 제거
		}
		catch (Exception e) {
			log.warn("Failed to parse S3 key from URL: {}", imageUrl);
			return imageUrl;
		}
	}

}
