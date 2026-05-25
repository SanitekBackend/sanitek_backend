package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Role;
import org.acme.domain.entity.User;
import org.acme.dto.request.CreateUserRequest;
import org.acme.dto.response.UserResponse;
import org.acme.exception.AppException;
import org.acme.mapper.UserMapper;
import org.acme.repository.RoleRepository;
import org.acme.repository.UserRepository;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject UserRepository userRepository;
    @Inject RoleRepository roleRepository;
    @Inject UserMapper userMapper;

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        if (userRepository.findByFirebaseUid(request.firebaseUid()).isPresent()) {
            throw AppException.conflict("User with this Firebase UID already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw AppException.conflict("User with this email already exists");
        }
        Role role = roleRepository.findByIdOptional(request.roleId())
                .orElseThrow(() -> AppException.notFound("Role not found"));

        User user = new User();
        user.setFirebaseUid(request.firebaseUid());
        user.setEmail(request.email());
        user.setRole(role);
        user.setIsActive(true);
        userRepository.persist(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse getByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(userMapper::toResponse)
                .orElseThrow(() -> AppException.notFound("User not found"));
    }

    @Transactional
    public UserResponse getById(Long id) {
        return userRepository.findByIdOptional(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> AppException.notFound("User not found"));
    }

    @Transactional
    public List<UserResponse> listAll() {
        return userRepository.listAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
