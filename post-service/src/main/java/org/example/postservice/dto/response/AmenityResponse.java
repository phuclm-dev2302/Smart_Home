package org.example.postservice.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class AmenityResponse {
    private UUID id;
    private UUID postDetailId;
    private String name;
    private String description;
}
