package com.agentsbackend.services;

import com.agentsbackend.DTO.response.NewsItemResponse;
import com.agentsbackend.exceptions.NewsFeedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class NewsService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final Pattern TICKER_PATTERN = Pattern.compile("[A-Z0-9.^=-]{1,15}");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiToken;

    public NewsService(
            ObjectMapper objectMapper,
            @Value("${news.marketaux-api-url:https://api.marketaux.com/v1/news/all}") String apiUrl,
            @Value("${news.marketaux-api-token:}") String apiToken) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
    }

    public List<NewsItemResponse> getNews(String ticker, Integer requestedLimit, String requestedSort) {
        String normalizedTicker = normalizeTicker(ticker);
        int limit = normalizeLimit(requestedLimit);
        String sort = normalizeSort(requestedSort);
        if (apiToken.isBlank()) {
            throw new NewsFeedException("MARKETAUX_API_KEY is not configured", null);
        }

        String query = "api_token=" + encode(apiToken)
                + "&symbols=" + encode(normalizedTicker)
                + "&language=en"
                + "&limit=" + limit
                + "&sort=" + sort;
        URI feedUri = URI.create(apiUrl + "?" + query);

        HttpRequest request = HttpRequest.newBuilder(feedUri)
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", "agents-backend/1.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new NewsFeedException("Marketaux returned HTTP " + response.statusCode(), null);
            }
            return parseResponse(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new NewsFeedException("The Marketaux news request was interrupted", exception);
        } catch (NewsFeedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new NewsFeedException("Unable to retrieve Marketaux news", exception);
        }
    }

    public List<NewsItemResponse> getNews(String ticker, Integer requestedLimit) {
        return getNews(ticker, requestedLimit, "latest");
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null || !TICKER_PATTERN.matcher(ticker.trim().toUpperCase(Locale.ROOT)).matches()) {
            throw new IllegalArgumentException("ticker must contain 1-15 letters, numbers, '.', '^', '=' or '-'");
        }
        return ticker.trim().toUpperCase(Locale.ROOT);
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        if (requestedLimit < 1 || requestedLimit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
        return requestedLimit;
    }

    private String normalizeSort(String requestedSort) {
        String sort = requestedSort == null ? "latest" : requestedSort.trim().toLowerCase(Locale.ROOT);
        return switch (sort) {
            case "latest" -> "published_at";
            case "popular" -> "relevance_score";
            default -> throw new IllegalArgumentException("sort must be latest or popular");
        };
    }

    private List<NewsItemResponse> parseResponse(String json) throws Exception {
        JsonNode data = objectMapper.readTree(json).path("data");
        List<NewsItemResponse> items = new ArrayList<>();
        for (JsonNode item : data) {
            items.add(new NewsItemResponse(
                    text(item, "title"),
                    text(item, "url"),
                    parsePublishedAt(text(item, "published_at")),
                    text(item, "description"),
                    text(item, "uuid"),
                    text(item, "source"),
                    item.path("relevance_score").isNumber() ? item.path("relevance_score").doubleValue() : null,
                    text(item, "image_url")));
        }
        return items;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private OffsetDateTime parsePublishedAt(String value) {
        try {
            return value == null ? null : OffsetDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}