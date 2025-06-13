package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.ChangeInfoRequest;
import org.example.userservice.dto.KeycloakUserInfo;
import org.example.userservice.dto.KeycloakUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final WebClient webClient;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.admin.username}")
    private String adminUsername;
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    @Value("${keycloak.admin.client-id}")
    private String adminClientId;
    @Value("${keycloak.admin.realm}")
    private String realm;
    @Value("${keycloak.admin.token-uri}")
    private String tokenAdminUri;
    @Value("${keycloak.admin.base-url}")
    private String baseUrl;

//    private String toFormData(Map<String, String> data) {
//        StringBuilder builder = new StringBuilder();
//        data.forEach((key, value) -> builder.append(key).append("=").append(value).append("&"));
//        // Remove the last "&"
//        builder.setLength(builder.length() - 1);
//        return builder.toString();
//    }
//
//    private Mono<String> getAdminToken() {
//        Map<String, String> formData = new HashMap<>();
//        formData.put("grant_type", "password");
//        formData.put("client_id", adminClientId);
//        formData.put("username", adminUsername);
//        formData.put("password", adminPassword);
//
//        return webClient.post()
//                .uri(tokenAdminUri)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//                .bodyValue(toFormData(formData))
//                .retrieve()
//                .bodyToMono(Map.class)
//                .map(response -> (String) response.get("access_token"));
//    }

    public Mono<KeycloakUserInfo> getUser(String accessToken) {
        return
               webClient.get()
                            .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(KeycloakUserInfo.class);
    }

    public Mono<String> getUserIdFromToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (Jwt) ctx.getAuthentication().getPrincipal())
                .map(Jwt::getSubject)
                .switchIfEmpty(Mono.error(new RuntimeException("No user ID found in token")))
                .onErrorMap(e -> new RuntimeException("Failed to get userId: " + e.getMessage()));
    }

    public Mono<KeycloakUserResponse> getUserByToken(String accessToken, ChangeInfoRequest request) {
        return getUserIdFromToken()
                .flatMap(userId -> {
                    // Build body update
                    Map<String, Object> updateBody = new HashMap<>();
                    updateBody.put("username", request.getUsername());
                    updateBody.put("firstName", request.getFirstName());
                    updateBody.put("lastName", request.getLastName());
                    updateBody.put("attributes", Map.of(
                            "phoneNumber", List.of(request.getPhoneNumber()),
                            "address", List.of(request.getAddress())
                    ));

                    // Gửi PUT request để cập nhật
                    return webClient.put()
                            .uri(baseUrl + "/admin/realms/"+ realm +"/users/{id}", userId)
                            .header("Authorization", "Bearer " + accessToken)
                            .bodyValue(updateBody)
                            .retrieve()
                            .toBodilessEntity()
                            .then(
                                    webClient.get()
                                            .uri(baseUrl + "/admin/realms/"+ realm +"/users/{id}", userId)
                                            .header("Authorization", "Bearer " + accessToken)
                                            .retrieve()
                                            .bodyToMono(KeycloakUserResponse.class)
                            );
                });
    }


}
