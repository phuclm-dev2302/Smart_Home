package org.example.notificationservice.controller;

import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getAllNotificationsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getAllNotificationsByUserId(userId));
    }
}
