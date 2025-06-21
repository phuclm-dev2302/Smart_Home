package org.example.controller;

import org.example.dto.ImageRequest;
import org.example.dto.ImageResponse;
import org.example.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<List<ImageResponse>> createImages(
            @RequestPart("img_url") Flux<FilePart> images,
            @RequestParam("postId") String postId
    ) {
        return imageService.createImages(images, UUID.fromString(postId));
    }

}
