package org.example.amenityservice.controller;

import org.example.amenityservice.dto.request.CreateAmenityListRequest;
import org.example.amenityservice.dto.request.CreateAmenityRequest;
import org.example.amenityservice.model.Amenity;
import org.example.amenityservice.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/amenities")
public class AmenityController {
    @Autowired
    private AmenityService amenityService;

    @PostMapping("")
    public ResponseEntity<List<Amenity>> createAmenities(@RequestBody CreateAmenityListRequest request) {
        return ResponseEntity.ok(amenityService.createAmenities(request));
    }

    @GetMapping("/post-detail/{id}")
    public ResponseEntity<List<Amenity>> getAmenitiesByPostDetailId(@PathVariable UUID id){
        return ResponseEntity.ok(amenityService.getByPostDetailId(id));
    }



}
