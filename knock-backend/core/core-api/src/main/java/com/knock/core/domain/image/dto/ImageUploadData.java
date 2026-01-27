package com.knock.core.domain.image.dto;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadData(MultipartFile file, String directory) {
}
