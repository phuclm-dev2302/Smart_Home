package org.example.authservice.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.LoginResponse;
import org.example.authservice.dto.RegisterRequest;
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
public class KeycloakTokenService {

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
    private String aminClientId;
    @Value("${keycloak.admin.realm}")
    private String realm;
    @Value("${keycloak.admin.token-uri}")
    private String tokenAdminUri;
    @Value("${keycloak.admin.base-url}")
    private String baseUrl;


    public Mono<LoginResponse> getToken(String username, String password) {
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("client_id", clientId);
        formData.put("client_secret", clientSecret);
        formData.put("username", username);
        formData.put("password", password);
        formData.put("scope", "openid");

        return webClient.post()
                .uri(tokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(toFormData(formData))
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }

    private String toFormData(Map<String, String> data) {
        StringBuilder builder = new StringBuilder();
        data.forEach((key, value) -> builder.append(key).append("=").append(value).append("&"));
        // Remove the last "&"
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    public Mono<String> register(RegisterRequest request) {
        return getAdminToken()
                .flatMap(token -> webClient.post()
                        .uri(baseUrl + "/admin/realms/" + realm + "/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(buildUserPayload(request))
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                String location = response.headers().header("Location").stream().findFirst().orElse(null);
                                if (location != null && location.contains("/users/")) {
                                    String userId = location.substring(location.lastIndexOf("/") + 1);
                                    // Gửi email xác nhận
                                    return sendVerificationEmail(token, userId)
                                            .thenReturn("✅ User created successfully. Verification email sent.");
                                } else {
                                    return Mono.error(new RuntimeException("User created but failed to extract ID"));
                                }
                            } else {
                                return response.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(new RuntimeException("Failed to register user: " + body)));
                            }
                        }));
    }

    private Mono<String> getAdminToken() {
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("client_id", aminClientId);
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

    private Map<String, Object> buildUserPayload(RegisterRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", req.getUsername());
        payload.put("email", req.getEmail());
        payload.put("firstName", req.getFirstName());
        payload.put("lastName", req.getLastName());
        payload.put("enabled", true);
        payload.put("emailVerified", false);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", req.getPassword());
        credentials.put("temporary", false);

        payload.put("credentials", List.of(credentials));
        return payload;
    }

    // Phương thức gửi email xác nhận
    private Mono<Void> sendVerificationEmail(String adminToken, String userId) {
        return webClient.put()
                .uri(baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/send-verify-email")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}