package org.example.aiservice.dto;

import lombok.Data;

@Data
public class AiSearchRequest {
    private String text;
    private boolean useMemory;
    private boolean reset;
}
