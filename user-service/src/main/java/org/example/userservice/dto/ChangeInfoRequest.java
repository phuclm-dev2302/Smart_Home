package org.example.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeInfoRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
}
