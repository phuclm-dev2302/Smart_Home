package org.example.postservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Builder
@Data
public class CreateAmenityListRequest {
    @NotNull(message = "postDetailId must not be null")
    private UUID postDetailId;

    private List<CreateAmenityRequest> amenities;
}
