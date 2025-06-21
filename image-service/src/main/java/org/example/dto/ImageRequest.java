package org.example.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;

import java.util.UUID;

@Data
@Builder
public class ImageRequest {
    private FilePart img_url;
    private UUID postId;
}
