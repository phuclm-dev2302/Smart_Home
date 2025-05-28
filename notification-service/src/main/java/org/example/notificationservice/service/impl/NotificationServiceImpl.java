package org.example.notificationservice.service.impl;

import org.example.commonevent.common.event.CreatePostEvent;
import org.example.notificationservice.enums.NotificationStatus;
import org.example.notificationservice.enums.NotificationType;
import org.example.notificationservice.module.Notification;
import org.example.notificationservice.repositoty.NotificationRepository;
import org.example.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @KafkaListener(topics = "notificationTopic", groupId = "notificationGroup")
    public void handleCreatePostEvent(CreatePostEvent event) {

        System.out.println("Received post event: " + event);
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .title("Bài viết mới đã được tạo")
                .content("Bài viết với ID : " + event.getId() + " đã được đăng.")
                .type(NotificationType.POST)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
