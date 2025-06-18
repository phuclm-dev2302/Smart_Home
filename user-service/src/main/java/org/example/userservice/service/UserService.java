package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.ChangeInfoRequest;
import org.example.userservice.dto.ChangePasswordRequest;
import org.example.userservice.dto.KeycloakUserInfo;
import org.example.userservice.dto.KeycloakUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final WebClient webClient;

    @Value("${keycloak.userinfo-uri}")
    private String userInfoUri;

    @Value(("${keycloak.admin.client-id}"))
    private String adminClientId;
    @Value("${keycloak.admin.username}")
    private String adminUsername;
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    @Value(("${keycloak.admin.token-uri}"))
    private String tokenAdminUri;

    @Value("${keycloak.admin.base-url}")
    private String baseUrl;
    @Value("${keycloak.admin.realm}")
    private String realmName;

    @Value("${keycloak.client.client-id}")
    private String clientId;
    @Value("${keycloak.client.client-secret}")
    private String clientSecret;
    @Value("${keycloak.client.token-uri}")
    private String tokenUri;
    private String toFormData(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();
        data.forEach((key, value) -> builder.append(key).append("=").append(value).append("&"));
        // Remove the last "&"
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }
    private Mono<String> getAdminToken() {
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("client_id", adminClientId);
        formData.put("username", adminUsername);
        formData.put("password", adminPassword);

        return webClient.post()
                .uri(tokenAdminUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(toFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }
    private Mono<String> getUserIdByUsername(String username, String adminToken) {
        return webClient.get()
                .uri(baseUrl+"/admin/realms/{realm}/users?username={username}", realmName, username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .bodyToFlux(Map.class)
                .next()
                .map(user -> (String) user.get("id"));
    }

    public Mono<KeycloakUserInfo> getUserInfo(String accessToken) {
        System.out.println("Chuẩn bị gửi request tới: " + userInfoUri);
        return webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KeycloakUserInfo.class);
    }

    public Mono<KeycloakUserInfo> updateUserInfo(String userId, ChangeInfoRequest request, String accessToken) {
        return getAdminToken()
                .flatMap(token ->
                        webClient.get()
                                .uri(baseUrl + "/admin/realms/{realm}/users/{id}", realmName, userId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .flatMap(currentUser -> {
                                    currentUser.put("firstName", request.getFirstName());
                                    currentUser.put("lastName", request.getLastName());
                                    currentUser.put("username", request.getUsername());

                                    Map<String, Object> attributes = new HashMap<>();
                                    attributes.put("phoneNumber", List.of(request.getPhoneNumber()));
                                    currentUser.put("attributes", attributes);

                                    return webClient.put()
                                            .uri(baseUrl + "/admin/realms/{realm}/users/{id}", realmName, userId)
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                            .bodyValue(currentUser)
                                            .retrieve()
                                            .toBodilessEntity()
                                            .then(getUserInfo(accessToken));
                                })
                );
    }
    public Mono<Void> changePassword(String username, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new RuntimeException("Mật khẩu xác nhận không khớp"));
        }

        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("client_id", clientId);
        formData.put("client_secret", clientSecret);
        formData.put("username", username);
        formData.put("password", request.getOldPassword());

        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(toFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(authResponse -> {
                    // Nếu xác thực thành công thì gọi admin API để đổi mật khẩu
                    return getAdminToken()
                            .flatMap(adminToken -> getUserIdByUsername(username, adminToken)
                                    .flatMap(userId -> {
                                        Map<String, Object> credentials = new HashMap<>();
                                        credentials.put("type", "password");
                                        credentials.put("value", request.getNewPassword());
                                        credentials.put("temporary", false);

                                        return webClient.put()
                                                .uri("http://localhost:8181/admin/realms/spring-boot-microservices-realm/users/{id}/reset-password",userId)
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                                .bodyValue(credentials)
                                                .retrieve()
                                                .toBodilessEntity()
                                                .then();
                                    })
                            );
                })
                .onErrorResume(ex -> {
                    System.err.println("Lỗi khi xác thực hoặc đổi mật khẩu: " + ex.getMessage());
                    return Mono.error(new RuntimeException("Mật khẩu cũ không đúng hoặc có lỗi xảy ra"));
                });
    }

}