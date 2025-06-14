package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.KeycloakUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final WebClient webClient;

    @Value("${keycloak.userinfo-uri}")
    private String userInfoUri;

    public Mono<KeycloakUserInfo> getUserInfo(String accessToken) {
        System.out.println("Chuẩn bị gửi request tới: " + userInfoUri);
        return webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KeycloakUserInfo.class);
    }
}