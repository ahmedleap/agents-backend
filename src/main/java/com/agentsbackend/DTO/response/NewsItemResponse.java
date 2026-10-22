package com.agentsbackend.DTO.response;

import java.time.OffsetDateTime;

public record NewsItemResponse(
        String title,
        String link,
        OffsetDateTime publishedAt,
        String description,
        String guid,
        String source,
        Double relevanceScore,
        String imageUrl) {
}