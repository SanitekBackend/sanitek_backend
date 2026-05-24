package org.acme.infrastructure.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class FirebaseConfig {

    private static final Logger LOG = Logger.getLogger(FirebaseConfig.class);

    @ConfigProperty(name = "firebase.credentials.path", defaultValue = "/firebase-service-account.json")
    String credentialsPath;

    @ConfigProperty(name = "firebase.enabled", defaultValue = "true")
    boolean enabled;

    void onStart(@Observes StartupEvent ev) throws IOException {
        if (!enabled) {
            LOG.warn("Firebase disabled (firebase.enabled=false).");
            return;
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        InputStream serviceAccount = openCredentials(credentialsPath);
        if (serviceAccount == null) {
            LOG.errorf("Firebase credentials not found at: %s", credentialsPath);
            return;
        }

        try (serviceAccount) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            LOG.info("Firebase initialized.");
        }
    }

    private InputStream openCredentials(String path) throws IOException {
        if (path == null || path.isBlank()) {
            path = "/firebase-service-account.json";
        }

        Path filesystemPath = Path.of(path);
        if (Files.exists(filesystemPath)) {
            return new FileInputStream(filesystemPath.toFile());
        }

        String resourcePath = path.startsWith("/") ? path : "/" + path;
        return getClass().getResourceAsStream(resourcePath);
    }
}
