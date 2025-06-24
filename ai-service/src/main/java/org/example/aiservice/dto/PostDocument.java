package org.example.aiservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Document(indexName = "post-index")
@JsonIgnoreProperties(ignoreUnknown = true)
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

    // 📊 Chi tiết - SỬA KIỂU DỮ LIỆU CHO ĐÚNG MAPPING
    private Double price;        // double thay vì String
    private Double area;         // double thay vì String

    private Integer bedRoom;     // integer thay vì String
    private Integer bathRoom;    // integer thay vì String
    private Integer floor;       // integer thay vì String

    private Boolean legalPapers; // boolean thay vì String

    // 🧰 Tiện ích
    private List<String> amenities;

    // ⚙️ Loại bài đăng + trạng thái
    private String postType;
    private String status;

    // 🕒 Thời gian
    private String createAt;  // LocalDate cho date mapping
}