package org.example.postservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PostDetailRequest {
    private BigDecimal price;
    private BigDecimal area;
    private String length;
    private String horizontal; // chieu rong
    private int bedRoom;  // so phong ngu
    private int bathRoom;  // so phong ve sinh
    private int floor;    // so tang
    private boolean legalPapers;  // giay to phap li( co/khong)
    private List<CreateAmenityRequest> amenities; // tiện ích thuộc PostDetail
}
