package org.example.notificationservice.service.impl;

import org.example.commonevent.common.event.CreatePostEvent;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.enums.NotificationStatus;
import org.example.notificationservice.enums.NotificationType;
import org.example.notificationservice.module.Notification;
import org.example.notificationservice.repositoty.NotificationRepository;
import org.example.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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

    @Override
    public NotificationResponse getNotificationById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found with id: " + id));
        notification.setStatus(NotificationStatus.READ);
        notification.setIsSeen(true);
        notification.setReadAt(LocalDateTime.now());
        return NotificationResponse.from(notification);
    }
    @Override
    public List<NotificationResponse> getAllNotificationsByUserId(UUID userId) {
        List<NotificationResponse> notifications = notificationRepository.findByUserId(userId).stream()
                .map(NotificationResponse::from)
                .toList();
        return notifications;
    }
}
