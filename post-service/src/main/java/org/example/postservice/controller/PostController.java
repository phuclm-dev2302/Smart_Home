package org.example.postservice.controller;

import org.example.postservice.dto.request.PostRequest;
import org.example.postservice.dto.request.UpdatePostRequest;
import org.example.postservice.dto.response.PostResponse;
import org.example.postservice.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
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
    public Mono<PostResponse> getPostById(@PathVariable UUID id) {
        return postService.getPostById(id);
    }

    @GetMapping("")
    public Mono<List<PostResponse>> getAllPost(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getAllPost(page, size);
    }

    @PutMapping("/{id}")
    public Mono<PostResponse> updatePost(@PathVariable("id") UUID id, @RequestBody UpdatePostRequest request){
        return postService.updatePost(id,request );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") UUID id){
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

}