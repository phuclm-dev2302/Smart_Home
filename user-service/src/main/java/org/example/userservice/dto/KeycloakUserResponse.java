package org.example.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class KeycloakUserResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private Map<String, List<String>> attributes;
}
