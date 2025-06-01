package org.example.commonevent.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostDocumentEvent {
    private UUID id;
    private String token;
    private String title;
    private String description;
    private String city;
    private String district;
    private String address;
    private String ward;
    private String price;
    private String area;
    private String bedRoom;
    private String bathRoom;
    private String floor;
    private String legalPapers;
    private List<String> amenities;
    private String postType;
    private String status;
    private String createAt;

}
