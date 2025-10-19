package com.ulog.backend.push;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.ulog.backend.domain.goal.UserPushToken;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.UserPushTokenRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final UserPushTokenRepository userPushTokenRepository;

    public PushNotificationService(UserPushTokenRepository userPushTokenRepository) {
        this.userPushTokenRepository = userPushTokenRepository;
    }

    public void sendToUser(User user, String title, String body) {
        if (!isFirebaseInitialized()) {
            log.warn("Firebase not initialized. Cannot send push notification.");
            return;
        }

        List<UserPushToken> tokens = userPushTokenRepository.findAllByUserAndIsActiveTrue(user);
        
        if (tokens.isEmpty()) {
            log.info("No active push tokens found for user {}", user.getId());
            return;
        }

        for (UserPushToken token : tokens) {
            try {
                sendNotification(token.getDeviceToken(), title, body);
                log.info("Push notification sent to user {} device {}", user.getId(), token.getId());
            } catch (Exception e) {
                log.error("Failed to send push notification to user {} device {}: {}", 
                         user.getId(), token.getId(), e.getMessage());
            }
        }
    }

    public void sendNotification(String deviceToken, String title, String body) {
        if (!isFirebaseInitialized()) {
            log.warn("Firebase not initialized. Cannot send push notification.");
            return;
        }

        try {
            Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

            Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
                .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("Successfully sent push notification: {}", response);
        } catch (Exception e) {
            log.error("Failed to send push notification to token {}: {}", deviceToken, e.getMessage());
            throw new RuntimeException("Failed to send push notification", e);
        }
    }

    private boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }
}

