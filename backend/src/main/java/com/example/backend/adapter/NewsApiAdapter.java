package com.example.backend.adapter;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NewsApiAdapter implements NewsSourceAdapter {

    @Value("${gnews.api.url:https://gnews.io/api/v4/search}")
    private String gNewsApiUrl;

    private final Environment environment;

    public NewsApiAdapter(Environment environment) {
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, String>> fetchSportsArticles() {
        String gNewsApiKey = resolveApiKey();
        if (gNewsApiKey.isBlank()) {
            return List.of();
        }

        URI uri = UriComponentsBuilder.fromUriString(gNewsApiUrl)
                .queryParam("q", "sports AND (cricket OR football OR stadium)")
                .queryParam("country", "in")
                .queryParam("token", gNewsApiKey)
                .queryParam("lang", "en")
                .encode()
                .build()
                .toUri();

        Map<String, Object> response;
        try {
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.getForObject(uri, Map.class);
        } catch (RestClientException ex) {
            return List.of();
        }

        if (response == null || !(response.get("articles") instanceof List<?> rawArticles)) {
            return List.of();
        }

        return rawArticles.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(this::adaptArticle)
                .toList();
    }

    private Map<String, String> adaptArticle(Map<?, ?> rawArticle) {
        return Map.of(
                "title", toSafeString(rawArticle.get("title")),
                "description", toSafeString(rawArticle.get("description")),
                "image", toSafeString(rawArticle.get("image")),
                "url", toSafeString(rawArticle.get("url")));
    }

    private String resolveApiKey() {
        String key = environment.getProperty("G_API_KEY");
        if (key == null || key.isBlank()) {
            key = environment.getProperty("GNEWS_API_KEY", "");
        }
        return key == null ? "" : key.trim();
    }

    private String toSafeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
