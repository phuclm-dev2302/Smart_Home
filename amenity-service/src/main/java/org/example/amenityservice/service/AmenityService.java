package org.example.amenityservice.service;

import org.example.amenityservice.dto.request.CreateAmenityListRequest;
import org.example.amenityservice.model.Amenity;

import java.util.List;
import java.util.UUID;


public interface AmenityService {
    List<Amenity> createAmenities(CreateAmenityListRequest request);
    List<Amenity> getByPostDetailId(UUID id);
}
