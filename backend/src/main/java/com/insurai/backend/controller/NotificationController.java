package com.insurai.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.insurai.backend.model.Notification;
import com.insurai.backend.service.InAppNotificationService;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class NotificationController {

    private final InAppNotificationService notificationService;

    public NotificationController(InAppNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // =====================================================
    // 🔹 GET ALL NOTIFICATIONS FOR USER
    // =====================================================
    // GET /notifications/user/{userId}?role=EMPLOYEE
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String role) {

        List<Notification> notifications;

        if (role != null) {
            notifications =
                    notificationService.getNotificationsByUserIdAndRole(userId, role);
        } else {
            notifications =
                    notificationService.getNotificationsByUserId(userId);
        }

        return ResponseEntity.ok(notifications);
    }

    // =====================================================
    // 🔹 GET UNREAD NOTIFICATIONS
    // =====================================================
    // GET /notifications/user/{userId}/unread?role=EMPLOYEE
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @PathVariable Long userId,
            @RequestParam(required = false) String role) {

        List<Notification> notifications;

        if (role != null) {
            notifications =
                    notificationService.getUnreadNotificationsByUserIdAndRole(userId, role);
        } else {
            notifications =
                    notificationService.getUnreadNotificationsByUserId(userId);
        }

        return ResponseEntity.ok(notifications);
    }

    // =====================================================
    // 🔹 GET NOTIFICATIONS BY ROLE (HR / ADMIN DASHBOARD)
    // =====================================================
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Notification>> getNotificationsByRole(
            @PathVariable String role) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByRole(role)
        );
    }

    // =====================================================
    // 🔹 MARK AS READ
    // =====================================================
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long notificationId) {

        Notification updated =
                notificationService.markAsRead(notificationId);

        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.notFound().build();
    }

    // =====================================================
    // 🔹 DELETE
    // =====================================================
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) {

        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
