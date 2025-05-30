package org.example.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import org.example.searchservice.dto.PostSearchRequest;
import org.example.searchservice.dto.PostDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostSearchService {

    private final ElasticsearchClient esClient;

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
}
