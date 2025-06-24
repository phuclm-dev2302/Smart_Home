package org.example.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PostLinkDto {
    private UUID id;
    private String url;
}
