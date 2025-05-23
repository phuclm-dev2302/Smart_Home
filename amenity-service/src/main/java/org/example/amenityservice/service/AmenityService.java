package org.example.amenityservice.service;

import org.example.amenityservice.dto.request.CreateAmenityListRequest;
import org.example.amenityservice.model.Amenity;

import java.util.List;


public interface AmenityService {
    List<Amenity> createAmenities(CreateAmenityListRequest request);
}
