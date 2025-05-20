package org.example.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.LoginResponse;
import org.example.authservice.dto.RegisterRequest;
import org.example.authservice.service.KeycloakTokenService;
import org.example.authservice.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TestController {

    private final KeycloakTokenService keycloakTokenService;

    @PostMapping("/login")
    public Mono<LoginResponse> callApiWithToken(@RequestBody LoginRequest loginRequest) {
        return keycloakTokenService.getToken(loginRequest.getUsername(), loginRequest.getPassword());
    }
    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody RegisterRequest request) {
        return keycloakTokenService.register(request)
                .map(response -> ResponseEntity.ok("User registered successfully"))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().body(error.getMessage())));
    }

}
