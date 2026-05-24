package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Company;
import org.acme.domain.entity.Role;
import org.acme.domain.entity.User;
import org.acme.dto.request.CreateAdminRequest;
import org.acme.dto.request.CreateCompanyUserRequest;
import org.acme.dto.request.UpdateAdminRequest;
import org.acme.dto.request.UpdateProfileRequest;
import org.acme.dto.response.UserResponse;
import org.acme.exception.AppException;
import org.acme.infrastructure.firebase.FirebaseUserService;
import org.acme.mapper.UserMapper;
import org.acme.repository.CompanyRepository;
import org.acme.repository.RoleRepository;
import org.acme.repository.UserRepository;

import java.util.List;

@ApplicationScoped
public class AdminManagementService {

    @Inject UserRepository userRepository;
    @Inject RoleRepository roleRepository;
    @Inject CompanyRepository companyRepository;
    @Inject UserMapper userMapper;
    @Inject FirebaseUserService firebaseUserService;
    @Inject CurrentUserService currentUserService;

    @Transactional
    public UserResponse createAdmin(CreateAdminRequest request) {
        User creator = currentUserService.requireSuperAdmin();
        Company company = companyRepository.findByIdOptional(request.companyId())
                .orElseThrow(() -> AppException.notFound("Company not found"));
        if (!Boolean.TRUE.equals(company.getIsActive())) {
            throw AppException.badRequest("Company is inactive");
        }
        if (userRepository.findActiveAdminByCompany(company.getId()).isPresent()) {
            throw AppException.conflict("Company already has an active admin");
        }

        Role adminRole = findRole(CurrentUserService.ROLE_ADMIN);
        return createFirebaseBackedUser(
                request.names(),
                request.email(),
                request.password(),
                adminRole,
                company,
                creator
        );
    }

    @Transactional
    public List<UserResponse> listAdmins() {
        currentUserService.requireSuperAdmin();
        return userRepository.findByRoleName(CurrentUserService.ROLE_ADMIN).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateAdmin(Long adminId, UpdateAdminRequest request) {
        currentUserService.requireSuperAdmin();
        User admin = userRepository.findByIdOptional(adminId)
                .orElseThrow(() -> AppException.notFound("Admin not found"));
        requireRole(admin, CurrentUserService.ROLE_ADMIN, "User is not an admin");

        String cleanEmail = request.email().trim().toLowerCase();
        userRepository.findByEmail(cleanEmail)
                .filter(existing -> !existing.getId().equals(admin.getId()))
                .ifPresent(existing -> {
                    throw AppException.conflict("User with this email already exists");
                });

        admin.setNames(request.names().trim());
        admin.setEmail(cleanEmail);
        firebaseUserService.updateProfile(admin.getFirebaseUid(), cleanEmail, admin.getNames());
        return userMapper.toResponse(admin);
    }

    @Transactional
    public void deactivateAdmin(Long adminId) {
        currentUserService.requireSuperAdmin();
        User admin = userRepository.findByIdOptional(adminId)
                .orElseThrow(() -> AppException.notFound("Admin not found"));
        requireRole(admin, CurrentUserService.ROLE_ADMIN, "User is not an admin");
        admin.setIsActive(false);
        firebaseUserService.setDisabled(admin.getFirebaseUid(), true);
    }

    @Transactional
    public UserResponse activateAdmin(Long adminId) {
        currentUserService.requireSuperAdmin();
        User admin = userRepository.findByIdOptional(adminId)
                .orElseThrow(() -> AppException.notFound("Admin not found"));
        requireRole(admin, CurrentUserService.ROLE_ADMIN, "User is not an admin");
        if (admin.getCompany() == null) {
            throw AppException.badRequest("Admin is not assigned to a company");
        }

        userRepository.findActiveAdminByCompany(admin.getCompany().getId())
                .filter(existing -> !existing.getId().equals(admin.getId()))
                .ifPresent(existing -> {
                    throw AppException.conflict("Company already has an active admin");
                });

        admin.setIsActive(true);
        firebaseUserService.setDisabled(admin.getFirebaseUid(), false);
        return userMapper.toResponse(admin);
    }

    @Transactional
    public UserResponse createCompanyUser(CreateCompanyUserRequest request) {
        User admin = currentUserService.requireAdmin();
        Role userRole = findRole(CurrentUserService.ROLE_USER);
        return createFirebaseBackedUser(
                request.names(),
                request.email(),
                request.password(),
                userRole,
                admin.getCompany(),
                admin
        );
    }

    @Transactional
    public List<UserResponse> listCompanyUsers() {
        User admin = currentUserService.requireAdmin();
        return userRepository.findByCompanyAndRoleName(admin.getCompany().getId(), CurrentUserService.ROLE_USER).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deactivateCompanyUser(Long userId) {
        User admin = currentUserService.requireAdmin();
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        requireCompanyUserAccess(admin, user);
        user.setIsActive(false);
        firebaseUserService.setDisabled(user.getFirebaseUid(), true);
    }

    @Transactional
    public UserResponse activateCompanyUser(Long userId) {
        User admin = currentUserService.requireAdmin();
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        requireCompanyUserAccess(admin, user);
        user.setIsActive(true);
        firebaseUserService.setDisabled(user.getFirebaseUid(), false);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse getMe() {
        return userMapper.toResponse(currentUserService.getCurrentUser());
    }

    @Transactional
    public UserResponse updateMe(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();
        String cleanEmail = request.email().trim().toLowerCase();
        userRepository.findByEmail(cleanEmail)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw AppException.conflict("User with this email already exists");
                });

        user.setNames(request.names().trim());
        user.setEmail(cleanEmail);
        firebaseUserService.updateProfile(user.getFirebaseUid(), cleanEmail, user.getNames());
        return userMapper.toResponse(user);
    }

    private UserResponse createFirebaseBackedUser(
            String names,
            String email,
            String password,
            Role role,
            Company company,
            User creator
    ) {
        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            throw AppException.conflict("User with this email already exists");
        }

        String firebaseUid = firebaseUserService.createUser(cleanEmail, password, names.trim());
        try {
            User user = new User();
            user.setNames(names.trim());
            user.setEmail(cleanEmail);
            user.setFirebaseUid(firebaseUid);
            user.setRole(role);
            user.setCompany(company);
            user.setCreatedBy(creator);
            user.setIsActive(true);
            userRepository.persistAndFlush(user);
            return userMapper.toResponse(user);
        } catch (RuntimeException e) {
            firebaseUserService.deleteUser(firebaseUid);
            throw e;
        }
    }

    private Role findRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> AppException.notFound("Role not found: " + roleName));
    }

    private void requireCompanyUserAccess(User admin, User user) {
        requireRole(user, CurrentUserService.ROLE_USER, "Target user is not a regular user");
        if (user.getCompany() == null || !admin.getCompany().getId().equals(user.getCompany().getId())) {
            throw AppException.forbidden("Cannot manage users from another company");
        }
    }

    private void requireRole(User user, String roleName, String message) {
        if (user.getRole() == null || !roleName.equals(user.getRole().getRoleName())) {
            throw AppException.badRequest(message);
        }
    }
}
