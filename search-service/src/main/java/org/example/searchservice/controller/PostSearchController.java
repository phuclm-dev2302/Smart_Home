package org.example.searchservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.searchservice.dto.PostDocument;
import org.example.searchservice.dto.PostSearchRequest;
import org.example.searchservice.service.PostSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService searchService;

    @PostMapping
    public ResponseEntity<List<PostDocument>> search(@RequestBody PostSearchRequest request) throws IOException {
        List<PostDocument> results = searchService.searchPosts(request);
        return ResponseEntity.ok(results);
    }
}
