package com.davidantasdev.fieldagentstracking.integration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GpsClient {

    private static final int PAGE_SIZE = 50;

    private final WebClient webClient;
    private final String baseUrl;
    private final String apiKey;

    public GpsClient(WebClient webClient,
                     @Value("${gps.api.base-url:https://desafio-media.onrender.com}") String baseUrl,
                     @Value("${gps.api.key:}") String apiKey) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public record AgentSyncResult(List<Map<String, Object>> agents, String nextSyncToken) {
        public static AgentSyncResult empty() {
            return new AgentSyncResult(List.of(), null);
        }
    }

    @CircuitBreaker(name = "gpsClientService", fallbackMethod = "fetchAgentsFallback")
    @Retry(name = "gpsClientRetry")
    public AgentSyncResult fetchAgentsFromExternalAPI(String lastSyncToken) {
        List<Map<String, Object>> allAgents = new ArrayList<>();
        String nextSyncToken = null;
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            String uri = buildUri(page, lastSyncToken);
            log.info("Fetching agents page {} from external API", page);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) break;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data != null && !data.isEmpty()) {
                allAgents.addAll(data);
            }

            // extract syncToken from response (various field names)
            for (String tokenKey : new String[]{"nextSyncToken", "syncToken", "next_sync_token"}) {
                if (response.containsKey(tokenKey)) {
                    nextSyncToken = (String) response.get(tokenKey);
                    break;
                }
            }

            // check pagination metadata
            @SuppressWarnings("unchecked")
            Map<String, Object> meta = response.containsKey("meta")
                    ? (Map<String, Object>) response.get("meta")
                    : (Map<String, Object>) response.get("pagination");

            if (meta != null) {
                Number totalPages = (Number) meta.get("totalPages");
                Number total = (Number) meta.getOrDefault("total", meta.get("count"));
                Object hasNextPage = meta.get("hasNextPage");

                if (Boolean.FALSE.equals(hasNextPage)) {
                    hasMore = false;
                } else if (totalPages != null && page >= totalPages.intValue()) {
                    hasMore = false;
                } else if (data == null || data.size() < PAGE_SIZE) {
                    hasMore = false;
                } else {
                    page++;
                }
            } else {
                // no pagination metadata — assume single page
                hasMore = false;
            }
        }

        log.info("Fetched {} total agents across {} page(s)", allAgents.size(), page);
        return new AgentSyncResult(allAgents, nextSyncToken);
    }

    // fallback when circuit breaker is open
    public AgentSyncResult fetchAgentsFallback(String lastSyncToken, Exception e) {
        log.warn("Circuit breaker open. Returning empty result. Error: {}", e.getMessage());
        return AgentSyncResult.empty();
    }

    private String buildUri(int page, String syncToken) {
        StringBuilder sb = new StringBuilder(baseUrl)
                .append("/agents?page=").append(page)
                .append("&limit=").append(PAGE_SIZE);
        if (syncToken != null && !syncToken.isBlank()) {
            sb.append("&syncToken=").append(syncToken);
        }
        return sb.toString();
    }
}