package org.example.aiservice.service;

import lombok.RequiredArgsConstructor;
import org.example.aiservice.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiSearchService {

    private final GeminiService geminiService;
    private final WebClient.Builder webClientBuilder;

    public Mono<AiSearchResponse> processQuery(AiSearchRequest query) {
        if (query.isReset()) {
            geminiService.resetConversation();
        }

        return geminiService.extractSearchRequest(query.getText(), query.isUseMemory())
                .flatMap(searchRequest ->
                        // Gọi search-service trả về list kết quả
                        webClientBuilder.build()
                                .post()
                                .uri("http://search-service/api/v1/search")
                                .bodyValue(searchRequest)
                                .retrieve()
                                .bodyToFlux(PostDocument.class)
                                .collectList()
                                .flatMap(results -> {
                                    // Tạo danh sách link
                                    List<PostLinkDto> postLinks = results.stream()
                                            .map(doc -> new PostLinkDto(
                                                    UUID.fromString(doc.getId()),
                                                    "http:///post-service/api/v1/posts/" + doc.getId()
                                            ))
                                            .toList();

                                    // Tóm tắt kết quả bằng AI
                                    return geminiService.summarizeSearchResult(query.getText(), results, query.isUseMemory())
                                            .map(summary -> AiSearchResponse.builder()
                                                    .extractedSearch(searchRequest)
                                                    .results(results)
                                                    .postLinks(postLinks)
                                                    .summary(summary)
                                                    .build());
                                })
                );
    }
}
