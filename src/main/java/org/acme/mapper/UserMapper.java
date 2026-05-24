package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.User;
import org.acme.dto.response.CompanySummary;
import org.acme.dto.response.RoleResponse;
import org.acme.dto.response.UserResponse;

@ApplicationScoped
public class UserMapper {

    public UserResponse toResponse(User user) {
        RoleResponse role = user.getRole() != null
                ? new RoleResponse(user.getRole().getId(), user.getRole().getRoleName())
                : null;
        CompanySummary company = user.getCompany() != null
                ? new CompanySummary(user.getCompany().getId(), user.getCompany().getCompanyName())
                : null;
        return new UserResponse(
                user.getId(),
                user.getNames(),
                user.getFirebaseUid(),
                user.getEmail(),
                user.getIsActive(),
                role,
                company
        );
    }
}
