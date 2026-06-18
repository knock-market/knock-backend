package com.knock.core.domain.itempolicy.dto;

import com.knock.core.enums.ItemType;

public record ItemPolicyWarningData(String title, String description, ItemType itemType) {
}
