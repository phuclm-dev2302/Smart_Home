package org.example.postservice.dto.request;

import lombok.Data;
import org.example.postservice.enums.PostTypeEnums;
import org.example.postservice.enums.StatusEnums;

import java.time.LocalDate;

@Data
public class UpdatePostRequest {
    private String title;
    private String description;
    private String address;
    private String city;
    private String district;
    private String ward;
    private PostTypeEnums postType;
    private StatusEnums status;
    private UpdatePostDetailRequest updatePostDetailRequest;
}
