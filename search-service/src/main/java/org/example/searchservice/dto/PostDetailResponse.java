package org.example.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponse {
    private String price;
    private String area;
    private String bedRoom;
    private String bathRoom;
    private String floor;
    private String legalPapers;
    private String length;
    private String horizontal;
    private List<AmenityResponse> amenities;
}
