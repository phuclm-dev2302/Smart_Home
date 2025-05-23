package org.example.amenityservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateAmenityListRequest {

    @NotNull(message = "postDetailId must not be null")
    private UUID postDetailId;

    private List<CreateAmenityRequest> amenities;
}
