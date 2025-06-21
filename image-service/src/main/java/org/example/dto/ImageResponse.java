package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Image;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponse {
    private UUID id;
    private UUID postId;
    private String imgUrls;
    private LocalDate createdAt;

    public static ImageResponse from(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .postId(image.getPostId())
                .imgUrls(image.getImageUrl())
                .createdAt(LocalDate.now())
                .build();
    }
}
