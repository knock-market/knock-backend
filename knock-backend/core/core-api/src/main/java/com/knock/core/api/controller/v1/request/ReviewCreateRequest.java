package com.knock.core.api.controller.v1.request;

public record ReviewCreateRequest(Long id, String content, int score) {
}
