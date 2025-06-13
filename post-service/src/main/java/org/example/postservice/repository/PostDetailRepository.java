package org.example.postservice.repository;

import org.example.postservice.model.Post;
import org.example.postservice.model.PostDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface PostDetailRepository extends JpaRepository<PostDetail, UUID> {
}
