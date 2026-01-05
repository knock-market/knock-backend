package com.knock.infra.s3.dto;

public record ImageUploadResult(String originalFilename, String imageUrl, String s3Key) {
}
