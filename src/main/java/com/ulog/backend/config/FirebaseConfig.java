package com.ulog.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config-path:classpath:firebase-service-account.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getServiceAccountStream();
                
                if (serviceAccount == null) {
                    log.warn("Firebase configuration file not found. Push notifications will be disabled.");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase has been initialized successfully");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
            log.warn("Push notifications will be disabled");
        }
    }

    private InputStream getServiceAccountStream() throws IOException {
        try {
            // 尝试从类路径加载
            if (firebaseConfigPath.startsWith("classpath:")) {
                String path = firebaseConfigPath.substring("classpath:".length());
                ClassPathResource resource = new ClassPathResource(path);
                if (resource.exists()) {
                    return resource.getInputStream();
                }
            } else {
                // 尝试从文件系统加载
                return new FileInputStream(firebaseConfigPath);
            }
        } catch (Exception e) {
            log.debug("Could not load Firebase config from {}: {}", firebaseConfigPath, e.getMessage());
        }
        return null;
    }
}

