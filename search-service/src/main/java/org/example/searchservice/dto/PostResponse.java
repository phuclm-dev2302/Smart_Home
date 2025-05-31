package org.example.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private UUID id;

    private String title;
    private String description;

    private String address;
    private String city;
    private String district;
    private String ward;

    private String postType;
    private String status;
    private String createAt;

    private PostDetailResponse postDetail;
}
