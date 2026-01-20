package com.knock.core.domain.group.dto;

import com.knock.core.api.controller.v1.request.GroupCreateRequestDto;

public record GroupCreateData(String name, String description) {
    public static GroupCreateData of(GroupCreateRequestDto request) {
        return new GroupCreateData(request.name(), request.description());
    }
}
