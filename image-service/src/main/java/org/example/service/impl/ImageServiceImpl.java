package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.ImageResponse;
import org.example.model.Image;
import org.example.repository.ImageRepository;
import org.example.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public Mono<List<ImageResponse>> getImagesByPostId(UUID postId) {
        return Mono.fromCallable(() -> {
            List<Image> images = imageRepository.findByPostId(postId);
            if (images.isEmpty()) {
                throw new RuntimeException("No images found");
            }
            return images.stream()
                    .map(ImageResponse::from)
                    .toList();
        });
    }

    @Override
    public Mono<List<ImageResponse>> createImages(Flux<FilePart> images, UUID postId) {
        Path uploadDir = Paths.get("image-service/uploads/image").toAbsolutePath();

        return images
                .flatMap(file -> {
                    try {
                        if (!Files.exists(uploadDir)) {
                            Files.createDirectories(uploadDir);
                        }
                    } catch (IOException e) {
                        return Mono.error(e);
                    }

                    String filename = System.currentTimeMillis() + "_" + file.filename();
                    Path path = uploadDir.resolve(filename);
                    String url = "/uploads/image/" + filename;

                    Image image = Image.builder()
                            .postId(postId)
                            .imageUrl(url)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return file.transferTo(path)
                            .then(Mono.fromCallable(() -> imageRepository.save(image)));
                })
                .cast(Image.class)
                .collectList()
                .map(savedImages -> savedImages.stream()
                        .map(image -> ImageResponse.builder()
                                .id(image.getId())
                                .postId(image.getPostId())
                                .imgUrls(image.getImageUrl())
                                .createdAt(image.getCreatedAt().toLocalDate())
                                .build())
                        .collect(Collectors.toList()));
    }
    @Override
    public Mono<Void> deleteImageById(UUID imageId) {
        return Mono.fromRunnable(() -> {
            imageRepository.findById(imageId).ifPresent(image -> {
                try {
                    Path path = Paths.get("image-service", image.getImageUrl());
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete image file", e);
                }
                imageRepository.deleteById(imageId);
            });
        });
    }
    @Override
    public Mono<Void> deleteImagesByPostId(UUID postId) {
        return Mono.fromRunnable(() -> {
            List<Image> images = imageRepository.findByPostId(postId);
            for (Image image : images) {
                try {
                    Path path = Paths.get("image-service", image.getImageUrl());
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete image file: " + image.getImageUrl(), e);
                }
                imageRepository.deleteById(image.getId());
            }
        });
    }
}
