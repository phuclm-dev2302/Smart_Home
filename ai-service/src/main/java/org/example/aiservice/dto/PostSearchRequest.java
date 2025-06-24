package org.example.aiservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostSearchRequest {
    // 📌 Must - filter bắt buộc (kiểu keyword / integer)
    private String city;         // keyword
    private String district;     // keyword
    private String postType;     // keyword

    private Integer bedRoom;     // integer
    private Integer bathRoom;    // integer

    // 📌 Should - tăng độ liên quan (kiểu text / range)
    private String title;        // text
    private String description;  // text

    private Double minPrice;     // double
    private Double maxPrice;     // double
    private Double minArea;      // double
    private Double maxArea;      // double

    private List<String> amenities;
}
