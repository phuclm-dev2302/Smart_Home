package org.example.postservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.postservice.model.PostDetail;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponse {
    private BigDecimal price;
    private BigDecimal area;
    private String length;
    private String horizontal;
    private int bedRoom;
    private int bathRoom;
    private int floor;
    private boolean legalPapers;

    private List<AmenityResponse> amenities;

    public static PostDetailResponse toDto(PostDetail postDetail, List<AmenityResponse> amenities) {
        PostDetailResponse response = new PostDetailResponse();
        response.setPrice(postDetail.getPrice());
        response.setArea(postDetail.getArea());
        response.setLength(postDetail.getLength());
        response.setHorizontal(postDetail.getHorizontal());
        response.setBedRoom(postDetail.getBedRoom());
        response.setBathRoom(postDetail.getBathRoom());
        response.setFloor(postDetail.getFloor());
        response.setLegalPapers(postDetail.isLegalPapers());
        response.setAmenities(amenities);
        return response;
    }


}
