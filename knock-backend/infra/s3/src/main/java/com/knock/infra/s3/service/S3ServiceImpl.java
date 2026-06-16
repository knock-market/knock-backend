package com.knock.infra.s3.service;

import com.knock.infra.s3.dto.ImageUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

	private static final String INVALID_IMAGE_URL_REASON = "Invalid image URL";

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

	private String extractKeyFromUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			throw invalidImageUrlException();
		}

		try {
			URI uri = URI.create(imageUrl);
			if (uri.getScheme() == null || uri.getHost() == null) {
				throw invalidImageUrlException();
			}

			String path = uri.getPath();
			if (path == null || path.isBlank() || "/".equals(path)) {
				throw invalidImageUrlException();
			}
			return path.substring(1);
		}
		catch (IllegalArgumentException e) {
			throw invalidImageUrlException();
		}
	}

	private ResponseStatusException invalidImageUrlException() {
		return new ResponseStatusException(BAD_REQUEST, INVALID_IMAGE_URL_REASON);
	}

}
