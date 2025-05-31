package org.example.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.example.commonevent.common.event.CreatePostDocumentEvent;
import org.example.searchservice.dto.PostSearchRequest;
import org.example.searchservice.dto.PostDocument;
import org.example.searchservice.dto.PostResponse;
import org.example.searchservice.dto.AmenityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostSearchService {

    private final ElasticsearchClient esClient;
    @Autowired
    private WebClient.Builder webClientBuilder;

    public PostSearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public List<PostDocument> searchPosts(PostSearchRequest request) throws IOException {
        Query finalQuery = buildQuery(request);

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("posts")
                .query(finalQuery)
        );

        SearchResponse<PostDocument> response = esClient.search(searchRequest, PostDocument.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    private Query buildQuery(PostSearchRequest request) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> shouldQueries = new ArrayList<>();

        // MUST: Các trường bắt buộc
        if (request.getCity() != null) {
            mustQueries.add(matchQuery("city", request.getCity()));
        }
        if (request.getDistrict() != null) {
            mustQueries.add(matchQuery("district", request.getDistrict()));
        }
        if (request.getBedRoom() != null) {
            mustQueries.add(matchQuery("bedRoom", request.getBedRoom())); // Integer
        }
        if (request.getBathRoom() != null) {
            mustQueries.add(matchQuery("bathRoom", request.getBathRoom())); // Integer
        }

        // SHOULD: Các trường tùy chọn
        if (request.getTitle() != null) {
            shouldQueries.add(matchQuery("title", request.getTitle()));
        }
        if (request.getDescription() != null) {
            shouldQueries.add(matchQuery("description", request.getDescription()));
        }
        if (request.getPostType() != null) {
            shouldQueries.add(matchQuery("postType", request.getPostType()));
        }

        // Script filter (vì không dùng được rangeQuery)
        if (request.getMinPrice() != null) {
            mustQueries.add(scriptQuery("price", ">=", request.getMinPrice()));
        }
        if (request.getMaxPrice() != null) {
            mustQueries.add(scriptQuery("price", "<=", request.getMaxPrice()));
        }
        if (request.getMinArea() != null) {
            mustQueries.add(scriptQuery("area", ">=", request.getMinArea()));
        }
        if (request.getMaxArea() != null) {
            mustQueries.add(scriptQuery("area", "<=", request.getMaxArea()));
        }

        return Query.of(q -> q.bool(b -> b
                .must(mustQueries)
                .should(shouldQueries)
                .minimumShouldMatch(shouldQueries.isEmpty() ? null : "1")
        ));
    }

    private Query matchQuery(String field, String value) {
        return Query.of(q -> q.match(m -> m.field(field).query(value)));
    }

    private Query matchQuery(String field, Integer value) {
        return Query.of(q -> q.match(m -> m.field(field).query(value)));
    }

    private Query matchQuery(String field, Double value) {
        return Query.of(q -> q.match(m -> m.field(field).query(value)));
    }

    private Query scriptQuery(String field, String operator, Object value) {
        String scriptSource = String.format("doc['%s'].value %s params.value", field, operator);
        return Query.of(q -> q.script(s -> s
                .script(script -> script
                        .source(scriptSource)
                        .lang("painless")
                        .params("value", JsonData.of(value))
                )
        ));
    }

    @KafkaListener(topics = "searchTopic", groupId = "searchGroup")
    public void handleCreatePostDocumentEvent(CreatePostDocumentEvent event) {
        log.info("Received post document event: id = {}", event.getId());

        try {
            // 1. Gọi post-service để lấy PostResponse (đồng bộ)
            PostResponse postResponse = webClientBuilder.build()
                    .get()
                    .uri("http://post-service/api/v1/posts/" + event.getId())
                    .headers(headers -> headers.setBearerAuth(event.getToken()))
                    .retrieve()
                    .bodyToMono(PostResponse.class)
                    .block();

            if (postResponse == null) {
                log.error("PostResponse is null for id {}", event.getId());
                return;
            }

            // 2. Tạo JSON body giống yêu cầu
            Map<String, Object> json = new HashMap<>();
            json.put("id", postResponse.getId());
            json.put("title", postResponse.getTitle());
            json.put("description", postResponse.getDescription());
            json.put("address", postResponse.getAddress());
            json.put("city", postResponse.getCity());
            json.put("district", postResponse.getDistrict());
            json.put("ward", postResponse.getWard());

            json.put("price", postResponse.getPostDetail().getPrice());
            json.put("area", postResponse.getPostDetail().getArea());
            json.put("bedRoom", postResponse.getPostDetail().getBedRoom());
            json.put("bathRoom", postResponse.getPostDetail().getBathRoom());
            json.put("floor", postResponse.getPostDetail().getFloor());
            json.put("legalPapers", postResponse.getPostDetail().getLegalPapers());

            json.put("amenities", postResponse.getPostDetail().getAmenities().stream()
                    .map(AmenityResponse::getName)
                    .collect(Collectors.toList()));

            json.put("postType", postResponse.getPostType());
            json.put("status", postResponse.getStatus());
            json.put("createAt", postResponse.getCreateAt().toString());

            // 3. Gửi tới Elasticsearch (đồng bộ)
            String esResponse = webClientBuilder.build()
                    .post()
                    .uri("http://localhost:9200/posts/_doc/" + event.getId() + "?refresh=wait_for")
                    .header("Content-Type", "application/json")
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Elasticsearch response: {}", esResponse);

        } catch (Exception e) {
            log.error("Error processing post document event", e);
        }
    }

}
