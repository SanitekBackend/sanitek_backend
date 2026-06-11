package org.acme.mapper;

import org.acme.domain.entity.Company;
import org.acme.domain.entity.Role;
import org.acme.domain.entity.User;
import org.acme.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper — conversión entidad → DTO")
class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    @DisplayName("toResponse con rol y empresa → todos los campos mapeados")
    void toResponse_conRolYEmpresa_mapeaTodo() throws Exception {
        Role    role    = rol(2L, "ADMIN");
        Company company = empresa(5L, "TEC Monterrey");
        User    user    = usuario(1L, "Juan Pérez", "uid_abc123", "juan@tec.mx", true, role, company);

        UserResponse resp = mapper.toResponse(user);

        assertEquals(1L,          resp.id());
        assertEquals("Juan Pérez", resp.names());
        assertEquals("uid_abc123", resp.firebaseUid());
        assertEquals("juan@tec.mx", resp.email());
        assertTrue(resp.isActive());

        assertNotNull(resp.role());
        assertEquals(2L,      resp.role().id());
        assertEquals("ADMIN", resp.role().roleName());

        assertNotNull(resp.company());
        assertEquals(5L,              resp.company().id());
        assertEquals("TEC Monterrey", resp.company().companyName());
    }

    @Test
    @DisplayName("toResponse con role null → role en response es null")
    void toResponse_rolNull_esNull() throws Exception {
        User user = usuario(2L, "María López", "uid_xyz", "maria@test.com", true, null, null);

        UserResponse resp = mapper.toResponse(user);

        assertNull(resp.role());
        assertNull(resp.company());
        assertEquals("María López", resp.names());
    }

    @Test
    @DisplayName("toResponse con company null → company en response es null")
    void toResponse_companyNull_esNull() throws Exception {
        Role role = rol(3L, "USER");
        User user = usuario(3L, "Pedro", "uid_pdr", "pedro@ex.com", false, role, null);

        UserResponse resp = mapper.toResponse(user);

        assertNotNull(resp.role());
        assertEquals("USER", resp.role().roleName());
        assertNull(resp.company());
        assertFalse(resp.isActive());
    }

    @Test
    @DisplayName("toResponse con isActive=false → se refleja en el DTO")
    void toResponse_inactivo() throws Exception {
        Role role = rol(1L, "USER");
        User user = usuario(4L, "Laura", "uid_lau", "laura@x.com", false, role, null);

        UserResponse resp = mapper.toResponse(user);

        assertFalse(resp.isActive());
    }

    @Test
    @DisplayName("toResponse con names null → names en response es null")
    void toResponse_namesNull() throws Exception {
        Role role = rol(1L, "VIEWER");
        User user = usuario(5L, null, "uid_no_name", "anon@x.com", true, role, null);

        UserResponse resp = mapper.toResponse(user);

        assertNull(resp.names());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Role rol(Long id, String name) throws Exception {
        Role r = new Role();
        r.setRoleName(name);
        setField(r, "id", id);
        return r;
    }

    private Company empresa(Long id, String nombre) throws Exception {
        Company c = new Company();
        c.setCompanyName(nombre);
        setField(c, "id", id);
        return c;
    }

    private User usuario(Long id, String names, String fbUid, String email,
                         boolean active, Role role, Company company) throws Exception {
        User u = new User();
        u.setNames(names);
        u.setFirebaseUid(fbUid);
        u.setEmail(email);
        u.setIsActive(active);
        u.setRole(role);
        u.setCompany(company);
        setField(u, "id", id);
        return u;
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
