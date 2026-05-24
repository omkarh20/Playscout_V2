package com.example.backend.controller;

import com.example.backend.service.NewsService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSportsNews() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "articles", newsService.getSportsNews()));
    }
}
