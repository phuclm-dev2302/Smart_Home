package org.example.notificationservice.service;


import org.example.commonevent.common.event.CreatePostEvent;
import org.example.notificationservice.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void handleCreatePostEvent(CreatePostEvent event);
    NotificationResponse getNotificationById(UUID id);
    List<NotificationResponse> getAllNotificationsByUserId(UUID userId);
}
