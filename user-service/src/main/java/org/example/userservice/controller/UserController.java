package org.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.ChangeInfoRequest;
import org.example.userservice.dto.KeycloakUserInfo;
import org.example.userservice.dto.KeycloakUserResponse;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/userinfo")
    public Mono<KeycloakUserInfo> getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String accessToken = authHeader.replace("Bearer ", "");
        return userService.getUser(accessToken);
    }
    @PutMapping()
    public Mono<KeycloakUserResponse> updateCurrentUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody ChangeInfoRequest request) {
        String accessToken = authHeader.replace("Bearer ", "");

        return userService.getUserByToken(accessToken, request);
    }
}
