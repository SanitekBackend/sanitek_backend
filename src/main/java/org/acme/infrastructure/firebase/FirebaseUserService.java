package org.acme.infrastructure.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.exception.AppException;

@ApplicationScoped
public class FirebaseUserService {

    public String createUser(String email, String password, String names) {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(names)
                .setDisabled(false);
        try {
            return FirebaseAuth.getInstance().createUser(request).getUid();
        } catch (FirebaseAuthException e) {
            throw AppException.badRequest("Firebase user could not be created: " + e.getMessage());
        }
    }

    public void deleteUser(String firebaseUid) {
        if (firebaseUid == null || firebaseUid.isBlank()) {
            return;
        }
        try {
            FirebaseAuth.getInstance().deleteUser(firebaseUid);
        } catch (FirebaseAuthException ignored) {
        }
    }

    public void setDisabled(String firebaseUid, boolean disabled) {
        if (firebaseUid == null || firebaseUid.isBlank()) {
            return;
        }
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(firebaseUid)
                .setDisabled(disabled);
        try {
            FirebaseAuth.getInstance().updateUser(request);
        } catch (FirebaseAuthException e) {
            throw AppException.badRequest("Firebase user status could not be updated: " + e.getMessage());
        }
    }

    public void updateProfile(String firebaseUid, String email, String names) {
        if (firebaseUid == null || firebaseUid.isBlank()) {
            return;
        }
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(firebaseUid)
                .setEmail(email)
                .setDisplayName(names);
        try {
            FirebaseAuth.getInstance().updateUser(request);
        } catch (FirebaseAuthException e) {
            throw AppException.badRequest("Firebase user profile could not be updated: " + e.getMessage());
        }
    }
}
