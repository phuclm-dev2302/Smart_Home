<<<<<<<< HEAD:image-service/src/main/java/org/example/module/Image.java
package org.example.module;
========
package org.example.model;
>>>>>>>> b3e7870e0edb94eb3989dbb390114aa3b84bfa5b:image-service/src/main/java/org/example/model/Image.java

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
