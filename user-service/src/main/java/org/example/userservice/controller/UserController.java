package org.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.KeycloakUserInfo;
import org.example.userservice.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Mono<KeycloakUserInfo> getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.replace("Bearer", "").trim();
        return userService.getUserInfo(token);
    }
}
