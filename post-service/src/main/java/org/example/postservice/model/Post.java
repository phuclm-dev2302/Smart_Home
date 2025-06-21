package org.example.postservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.postservice.enums.PostTypeEnums;
import org.example.postservice.enums.StatusEnums;

import java.time.LocalDate;
import java.util.UUID;

@Table(name = "posts")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID postDetailId;

    @Column(nullable = false)
    private UUID userId;

    private String title;
    private String description;
    private String address;
    private String city;
    private String district;
    private String ward;
    private PostTypeEnums postType;
    private StatusEnums status;
    private LocalDate createAt;
    private LocalDate updatedAt;

}
