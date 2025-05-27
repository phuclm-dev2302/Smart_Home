package org.example.notificationservice.service;


import org.example.commonevent.common.event.CreatePostEvent;

public interface NotificationService {
    void handleCreatePostEvent(CreatePostEvent event);
}
