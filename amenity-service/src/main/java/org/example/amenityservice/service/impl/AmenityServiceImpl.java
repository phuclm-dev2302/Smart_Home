package org.example.amenityservice.service.impl;

import org.example.amenityservice.dto.request.CreateAmenityListRequest;
import org.example.amenityservice.dto.request.CreateAmenityRequest;
import org.example.amenityservice.model.Amenity;
import org.example.amenityservice.repository.AmenityRepository;
import org.example.amenityservice.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AmenityServiceImpl implements AmenityService {
    @Autowired
    private AmenityRepository amenityRepository;


    @Override
    public List<Amenity> createAmenities(CreateAmenityListRequest request) {
        List<Amenity> amenities = request.getAmenities().stream()
                .map(a -> Amenity.builder()
                        .postDetailId(request.getPostDetailId())
                        .name(a.getName())
                        .description(a.getDescription())
                        .build())
                .collect(Collectors.toList());

        return amenityRepository.saveAll(amenities);
    }
}
