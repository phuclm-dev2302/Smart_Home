package org.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KeycloakUserInfo {
    private String sub;
    @JsonProperty("preferred_username")
    private String preferredUsername;
    private String name;
    private String email;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String address;
}
