package org.example.notificationservice.service.impl;

import org.example.commonevent.common.event.CreatePostEvent;
import org.example.notificationservice.repositoty.NotificationRepository;
import org.example.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @KafkaListener(topics = "notificationTopic", groupId = "notificationGroup")
    public void handleCreatePostEvent(CreatePostEvent event) {
        System.out.println("Received post event: " + event);
    }


}
