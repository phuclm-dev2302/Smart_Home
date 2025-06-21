package org.example.repository;

import org.example.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    List<Image> findByPostId(UUID postId);
}
