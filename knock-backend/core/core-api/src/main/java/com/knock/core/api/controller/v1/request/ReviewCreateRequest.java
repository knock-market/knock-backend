package com.knock.core.api.controller.v1.request;

public record ReviewCreateRequest(Long itemId, String content, int score) {
}
