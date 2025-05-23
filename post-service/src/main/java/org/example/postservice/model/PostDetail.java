package org.example.postservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "post_details")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PostDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private BigDecimal price;
    private BigDecimal area;
    private String length;
    private String horizontal; // chieu rong
    private int bedRoom;  // so phong ngu
    private int bathRoom;  // so phong ve sinh
    private int floor;    // so tang
    private boolean legalPapers;  // giay to phap li( co/khong)
}
