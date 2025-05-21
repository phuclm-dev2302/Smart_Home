package org.example.postservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Table(name = "amenitíe")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID postDetailId;

    private String name;   // Wifi, Dieu hoa
    private String description; // Wifi 24/7


}
