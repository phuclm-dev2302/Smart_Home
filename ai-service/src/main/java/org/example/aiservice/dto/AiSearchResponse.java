package org.example.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class AiSearchResponse {
    private PostSearchRequest extractedSearch;
    private List<PostDocument> results;       // Dữ liệu gốc từ Elasticsearch
    private List<PostLinkDto> postLinks;      // Danh sách id + url
    private String summary;                   // Đoạn văn AI tóm tắt
}
