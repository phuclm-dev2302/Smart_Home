package org.example.postservice.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.postservice.enums.PostTypeEnums;
import org.example.postservice.enums.StatusEnums;
import org.example.postservice.model.Post;
import org.example.postservice.model.PostDetail;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class PostResponse {
    private UUID id;

    private UUID postDetailId;

    private UUID userId;

    private UUID imageId;

    private String title;
    private String description;
    private String address;
    private String city;
    private String district;
    private String ward;
    private PostTypeEnums postType;
    private StatusEnums status;
    private LocalDate createAt;
    private LocalDate updatedAt;

    private PostDetailResponse postDetail;

    public static PostResponse toDto(Post post, PostDetail postDetail, List<AmenityResponse> amenities) {
        return PostResponse.builder()
                .id(post.getId())
                .postDetailId(post.getPostDetailId())
                .userId(post.getUserId())
                .imageId(post.getImageId())
                .title(post.getTitle())
                .description(post.getDescription())
                .address(post.getAddress())
                .city(post.getCity())
                .district(post.getDistrict())
                .ward(post.getWard())
                .postType(post.getPostType())
                .status(post.getStatus())
                .createAt(post.getCreateAt())
                .updatedAt(post.getUpdatedAt())
                .postDetail(PostDetailResponse.toDto(postDetail, amenities))
                .build();
    }

}
