package org.example.postservice.controller;

import org.example.postservice.dto.request.PostRequest;
import org.example.postservice.dto.response.PostResponse;
import org.example.postservice.model.Post;
import org.example.postservice.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/user-id")
    public Mono<String> getUserId() {
        return postService.getUserIdFromToken()
                .map(userId -> userId);
    }
    @PostMapping("")
    public Mono<PostResponse> createPost(@RequestBody PostRequest request) {
        return postService.createPost(request);
    }
    @GetMapping("/{id}")
    public Mono<PostResponse> getPostById(@PathVariable UUID id){
        return postService.getPostById(id);
    }

}