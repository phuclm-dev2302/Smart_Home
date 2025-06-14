package org.example.userservice.dto;

import lombok.Data;

@Data
public class KeycloakUserInfo {
    private String sub;
    private String preferred_username;
    private String name;
    private String email;
    private String phoneNumber;
}
