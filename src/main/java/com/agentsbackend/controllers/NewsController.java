package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.NewsItemResponse;
import com.agentsbackend.services.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@Tag(name = "News", description = "Marketaux financial news for instruments")
@SecurityRequirement(name = "bearerAuth")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    @Operation(summary = "Get instrument news", description = "Returns recent or most relevant Marketaux news articles for a ticker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "News items returned"),
            @ApiResponse(responseCode = "400", description = "Invalid ticker or limit"),
            @ApiResponse(responseCode = "502", description = "Marketaux is unavailable or not configured")
    })
    public ResponseEntity<List<NewsItemResponse>> getNews(
            @Parameter(description = "Instrument ticker", example = "AAPL", required = true)
            @RequestParam String ticker,
            @Parameter(description = "Maximum number of items to return", example = "10")
            @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "Sort order: latest or popular", example = "popular")
            @RequestParam(defaultValue = "latest") String sort) {
        try {
            return ResponseEntity.ok(newsService.getNews(ticker, limit, sort));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}