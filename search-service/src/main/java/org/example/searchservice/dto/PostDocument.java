package org.example.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "post")
public class PostDocument {
    private String id;

    // 🏡 Thông tin cơ bản
    private String title;
    private String description;

    // 📍 Địa chỉ
    private String address;
    private String city;
    private String district;
    private String ward;

    // 📊 Chi tiết
    private String price;
    private String area;

    private String bedRoom;
    private String bathRoom;
    private String floor;

    private String legalPapers; // true/false dạng string

    // 🧰 Tiện ích
    private List<String> amenities;

    // ⚙️ Loại bài đăng + trạng thái
    private String postType;
    private String status;

    // 🕒 Thời gian
    private String createAt;
}
