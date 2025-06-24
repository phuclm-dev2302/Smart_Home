package org.example.service;

import org.example.dto.ImageResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ImageService {
    Mono<List<ImageResponse>> getImagesByPostId(UUID postId);
    Mono<List<ImageResponse>> createImages(Flux<FilePart> images, UUID postId);
    Mono<Void> deleteImageById(UUID imageId); // xoá 1 ảnh
    Mono<Void> deleteImagesByPostId(UUID postId); // xoá tất cả ảnh theo post
}