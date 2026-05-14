package org.acme.infrastructure.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class FirebaseConfig {

    private static final Logger LOG = Logger.getLogger(FirebaseConfig.class);

    @ConfigProperty(name = "firebase.credentials.path", defaultValue = "/firebase-service-account.json")
    String credentialsPath;

    @ConfigProperty(name = "firebase.enabled", defaultValue = "true")
    boolean enabled;

    void onStart(@Observes StartupEvent ev) throws IOException {
        if (!enabled) {
            LOG.warn("Firebase deshabilitado (firebase.enabled=false). Actívalo cuando tengas el archivo de credenciales.");
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = getClass().getResourceAsStream(credentialsPath);
            if (serviceAccount == null) {
                LOG.errorf("Firebase credentials no encontradas en: %s. Coloca firebase-service-account.json en src/main/resources/", credentialsPath);
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            LOG.info("Firebase inicializado correctamente.");
        }
    }
}
