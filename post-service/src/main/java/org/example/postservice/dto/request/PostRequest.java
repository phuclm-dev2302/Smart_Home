package org.example.postservice.dto.request;

import lombok.Data;
import org.example.postservice.enums.PostTypeEnums;

import java.util.List;
import java.util.UUID;

@Data
public class PostRequest {

    private String title;
    private String description;
    private String address;
    private String city;
    private String district;
    private String ward;
    private PostTypeEnums postType;
    private PostDetailRequest postDetailRequest; // chi tiết + tiện ích

}
