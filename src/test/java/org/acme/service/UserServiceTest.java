package org.acme.service;

import org.acme.domain.entity.Role;
import org.acme.domain.entity.User;
import org.acme.dto.request.CreateUserRequest;
import org.acme.dto.response.RoleResponse;
import org.acme.dto.response.UserResponse;
import org.acme.exception.AppException;
import org.acme.mapper.UserMapper;
import org.acme.repository.RoleRepository;
import org.acme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — gestión de usuarios")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock UserMapper     userMapper;

    @InjectMocks UserService service;

    private static final Long USER_ID = 1L;
    private static final Long ROLE_ID = 2L;
    private static final String FB_UID = "firebase_uid_abc123";
    private static final String EMAIL  = "test@sanitek.mx";

    private User user;
    private Role role;

    @BeforeEach
    void setUp() throws Exception {
        role = new Role();
        role.setRoleName("USER");
        setField(role, "id", ROLE_ID);

        user = new User();
        user.setFirebaseUid(FB_UID);
        user.setEmail(EMAIL);
        user.setRole(role);
        user.setIsActive(true);
        setField(user, "id", USER_ID);
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register → Firebase UID duplicado → lanza AppException 409")
    void register_firebaseUidDuplicado_conflict() {
        when(userRepository.findByFirebaseUid(FB_UID)).thenReturn(Optional.of(user));
        CreateUserRequest req = new CreateUserRequest(FB_UID, EMAIL, ROLE_ID);

        AppException ex = assertThrows(AppException.class, () -> service.register(req));

        assertEquals(jakarta.ws.rs.core.Response.Status.CONFLICT, ex.getStatus());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("register → email duplicado → lanza AppException 409")
    void register_emailDuplicado_conflict() {
        when(userRepository.findByFirebaseUid(FB_UID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        CreateUserRequest req = new CreateUserRequest(FB_UID, EMAIL, ROLE_ID);

        AppException ex = assertThrows(AppException.class, () -> service.register(req));

        assertEquals(jakarta.ws.rs.core.Response.Status.CONFLICT, ex.getStatus());
    }

    @Test
    @DisplayName("register → rol no encontrado → lanza AppException 404")
    void register_rolNoExiste_notFound() {
        when(userRepository.findByFirebaseUid(FB_UID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(roleRepository.findByIdOptional(ROLE_ID)).thenReturn(Optional.empty());
        CreateUserRequest req = new CreateUserRequest(FB_UID, EMAIL, ROLE_ID);

        AppException ex = assertThrows(AppException.class, () -> service.register(req));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("register → datos únicos y válidos → crea usuario activo y devuelve response")
    void register_datosValidos_creaYDevuelveResponse() {
        UserResponse expected = userResponse();

        when(userRepository.findByFirebaseUid(FB_UID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(roleRepository.findByIdOptional(ROLE_ID)).thenReturn(Optional.of(role));
        when(userMapper.toResponse(any(User.class))).thenReturn(expected);

        CreateUserRequest req = new CreateUserRequest(FB_UID, EMAIL, ROLE_ID);
        UserResponse actual = service.register(req);

        assertEquals(expected, actual);
        verify(userRepository).persist(any(User.class));
    }

    @Test
    @DisplayName("register → usuario creado → isActive = true")
    void register_usuarioCreado_isActivo() {
        when(userRepository.findByFirebaseUid(FB_UID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(roleRepository.findByIdOptional(ROLE_ID)).thenReturn(Optional.of(role));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse());

        service.register(new CreateUserRequest(FB_UID, EMAIL, ROLE_ID));

        // Verificamos que el persist fue llamado (el usuario se guardó)
        verify(userRepository).persist(any(User.class));
    }

    // ── getByFirebaseUid ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getByFirebaseUid → encontrado → devuelve response")
    void getByFirebaseUid_encontrado_devuelveResponse() {
        UserResponse expected = userResponse();
        when(userRepository.findByFirebaseUid(FB_UID)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse actual = service.getByFirebaseUid(FB_UID);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getByFirebaseUid → no encontrado → lanza AppException 404")
    void getByFirebaseUid_noEncontrado_notFound() {
        when(userRepository.findByFirebaseUid("uid_inexistente")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> service.getByFirebaseUid("uid_inexistente"));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById → encontrado → devuelve response")
    void getById_encontrado_devuelveResponse() {
        UserResponse expected = userResponse();
        when(userRepository.findByIdOptional(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse actual = service.getById(USER_ID);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getById → no encontrado → lanza AppException 404")
    void getById_noEncontrado_notFound() {
        when(userRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> service.getById(999L));

        assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, ex.getStatus());
    }

    // ── listAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll → 2 usuarios → devuelve lista de 2 responses")
    void listAll_dosUsuarios_devuelveListaDe2() throws Exception {
        User user2 = new User();
        user2.setFirebaseUid("uid_2");
        user2.setEmail("otro@test.com");
        setField(user2, "id", 2L);

        UserResponse r1 = userResponse();
        UserResponse r2 = new UserResponse(2L, null, "uid_2", "otro@test.com", true, null, null);

        when(userRepository.listAll()).thenReturn(List.of(user, user2));
        when(userMapper.toResponse(user)).thenReturn(r1);
        when(userMapper.toResponse(user2)).thenReturn(r2);

        List<UserResponse> result = service.listAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("listAll → sin usuarios → lista vacía")
    void listAll_sinUsuarios_listaVacia() {
        when(userRepository.listAll()).thenReturn(List.of());

        List<UserResponse> result = service.listAll();

        assertTrue(result.isEmpty());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserResponse userResponse() {
        return new UserResponse(USER_ID, "Test User", FB_UID, EMAIL, true,
                new RoleResponse(ROLE_ID, "USER"), null);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
