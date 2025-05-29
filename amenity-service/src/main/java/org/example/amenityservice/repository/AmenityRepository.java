package org.example.amenityservice.repository;

import org.example.amenityservice.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, UUID> {
    @Query("SELECT a FROM Amenity a " +
            "WHERE a.postDetailId = :postDetailId")
    List<Amenity> findAllByPostDetailId(@Param("postDetailId") UUID postDetailId);

}
