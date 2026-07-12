package com.nokcha.efbe.common.init;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class FcmInitializer {

    private final ResourceLoader resourceLoader;

    @Value("${fcm.firebase.config.path}")
    private String firebaseConfigPath;

    @Value("${firebase.project-id}")
    private String firebaseProjectId;

    // firebase.enabled=false (테스트/로컬 등) 이면 FCM 초기화 스킵. 운영은 기본 true.
    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) return;
        if (isDefaultAppInitialized()) return;

        Resource firebaseConfig = resourceLoader.getResource(firebaseConfigPath);
        if (!firebaseConfig.exists()) {
            throw new IllegalStateException("Firebase config file does not exist: " + firebaseConfigPath);
        }

        try (InputStream inputStream = firebaseConfig.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .setProjectId(firebaseProjectId)
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize Firebase Admin SDK", e);
        }
    }

    private boolean isDefaultAppInitialized() {
        return FirebaseApp.getApps().stream()
                .anyMatch(app -> FirebaseApp.DEFAULT_APP_NAME.equals(app.getName()));
    }
}