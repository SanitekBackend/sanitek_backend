package org.acme.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.User;
import org.acme.dto.request.LoginRequest;
import org.acme.dto.response.LoginResponse;
import org.acme.exception.AppException;
import org.acme.mapper.UserMapper;
import org.acme.repository.UserRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ApplicationScoped
public class AuthService {

    private static final String FIREBASE_SIGN_IN_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=";

    @ConfigProperty(name = "firebase.web-api-key", defaultValue = "not-configured")
    String firebaseWebApiKey;

    @Inject ObjectMapper objectMapper;
    @Inject UserRepository userRepository;
    @Inject UserMapper userMapper;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (firebaseWebApiKey == null || firebaseWebApiKey.isBlank() || "not-configured".equals(firebaseWebApiKey)) {
            throw AppException.badRequest("Firebase web API key is not configured");
        }

        JsonNode firebaseResponse = signInWithFirebase(request.email().trim().toLowerCase(), request.password());
        String firebaseUid = text(firebaseResponse, "localId");
        User user = userRepository.findAuthenticatedByFirebaseUid(firebaseUid)
                .orElseThrow(() -> AppException.forbidden("Authenticated Firebase user is not registered"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw AppException.forbidden("User is inactive");
        }

        return new LoginResponse(
                text(firebaseResponse, "idToken"),
                text(firebaseResponse, "refreshToken"),
                Long.parseLong(text(firebaseResponse, "expiresIn")),
                userMapper.toResponse(user)
        );
    }

    private JsonNode signInWithFirebase(String email, String password) {
        try {
            String body = objectMapper.writeValueAsString(new FirebaseLoginPayload(email, password, true));
            String apiKey = URLEncoder.encode(firebaseWebApiKey.trim(), StandardCharsets.UTF_8);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(FIREBASE_SIGN_IN_URL + apiKey))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());
            if (response.statusCode() >= 400) {
                throw AppException.unauthorized(firebaseErrorMessage(json));
            }
            return json;
        } catch (IOException e) {
            throw AppException.badRequest("Firebase login response could not be processed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw AppException.badRequest("Firebase login request was interrupted");
        }
    }

    private String firebaseErrorMessage(JsonNode json) {
        String message = json.path("error").path("message").asText("Invalid Firebase credentials");
        return switch (message) {
            case "EMAIL_NOT_FOUND", "INVALID_PASSWORD", "INVALID_LOGIN_CREDENTIALS" -> "Invalid email or password";
            case "USER_DISABLED" -> "Firebase user is disabled";
            case "API_KEY_INVALID" -> "Firebase web API key is invalid";
            default -> "Firebase login failed: " + message;
        };
    }

    private String text(JsonNode node, String fieldName) {
        String value = node.path(fieldName).asText(null);
        if (value == null || value.isBlank()) {
            throw AppException.badRequest("Firebase login response is missing field: " + fieldName);
        }
        return value;
    }

    private record FirebaseLoginPayload(String email, String password, boolean returnSecureToken) {
    }
}
