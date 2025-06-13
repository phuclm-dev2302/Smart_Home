package org.example.postservice.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePostDetailRequest {
    private BigDecimal price;
    private BigDecimal area;
    private String length;
    private String horizontal; // chieu rong
    private int bedRoom;  // so phong ngu
    private int bathRoom;  // so phong ve sinh
    private int floor;    // so tang
    private boolean legalPapers;  // gi
}
