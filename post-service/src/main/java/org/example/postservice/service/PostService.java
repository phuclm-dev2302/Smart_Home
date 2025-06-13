package org.example.postservice.service;

import org.example.postservice.dto.request.PostRequest;
import org.example.postservice.dto.request.UpdatePostRequest;
import org.example.postservice.dto.response.PostResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface PostService {
    Mono<String> getUserIdFromToken();
    Mono<PostResponse> createPost(PostRequest request);
    Mono<PostResponse> getPostById(UUID id);
    Mono<List<PostResponse>> getAllPost(int page, int size);
    Mono<PostResponse> updatePost(UUID id, UpdatePostRequest request);
    void deletePost(UUID id);
}