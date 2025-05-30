package org.example.searchservice.dto;

import lombok.Data;
@Data
public class PostSearchRequest {
    // 📌 Must - filter bắt buộc
    private String city;
    private String district;
    private String postType;

    // 📌 Should - điều kiện tăng độ liên quan
    private String title;
    private String description;
    private String minPrice;
    private String maxPrice;
    private String minArea;
    private String maxArea;
    private String bedRoom;
    private String bathRoom;
}
