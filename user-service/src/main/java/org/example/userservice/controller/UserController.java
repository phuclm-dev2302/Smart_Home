package org.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.ChangeInfoRequest;
import org.example.userservice.dto.ChangePasswordRequest;
import org.example.userservice.dto.KeycloakUserInfo;
import org.example.userservice.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Mono<KeycloakUserInfo> getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.replace("Bearer", "").trim();
        return userService.getUserInfo(token);
    }
    @PutMapping("{id}")
    public Mono<ResponseEntity<KeycloakUserInfo>> updateUser(@PathVariable String id,
                                                             @RequestBody ChangeInfoRequest request,
                                                             @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        return userService.updateUserInfo(id, request, token)
                .map(ResponseEntity::ok);
    }
    @PutMapping("/change-password")
    public Mono<ResponseEntity<Map<String, String>>> changePassword(
            @RequestParam("username") String username,
            @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(username, request)
                .thenReturn(ResponseEntity.ok().body(
                        Map.of("message", "Update password successfully")
                ));
    }
}
