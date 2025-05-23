package org.example.amenityservice.controller;

import org.example.amenityservice.dto.request.CreateAmenityListRequest;
import org.example.amenityservice.dto.request.CreateAmenityRequest;
import org.example.amenityservice.model.Amenity;
import org.example.amenityservice.service.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/amenities")
public class AmenityController {
    @Autowired
    private AmenityService amenityService;

    @PostMapping("")
    public ResponseEntity<List<Amenity>> createAmenities(@RequestBody CreateAmenityListRequest request) {
        return ResponseEntity.ok(amenityService.createAmenities(request));
    }



}
