package org.example.postservice.service;

import org.example.postservice.dto.request.PostDetailRequest;
import org.example.postservice.dto.request.PostRequest;
import org.example.postservice.dto.response.PostResponse;
import org.example.postservice.model.Post;
import reactor.core.publisher.Mono;

public interface PostService {
    Mono<String> getUserIdFromToken();
    Mono<PostResponse> createPost(PostRequest request);
}