package org.example.postservice.repository;

import org.example.postservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Optional<Post> findById(UUID id);

    List<Post> findByUserId(UUID id);

}
