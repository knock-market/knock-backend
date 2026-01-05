package com.knock.infra.s3.service;

import com.knock.infra.s3.dto.ImageUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface S3Service {

	ImageUploadResult uploadImage(MultipartFile file, String directory);

	void deleteImage(String imageUrl);

}
