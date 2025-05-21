package org.example.postservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "images")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID postId;

    private String img_url;
    private LocalDateTime createdAt;

}
