package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.*;
import org.acme.dto.request.*;
import org.acme.dto.response.*;
import org.acme.exception.AppException;
import org.acme.mapper.AlcaldiaMapper;
import org.acme.mapper.UsuarioMapper;
import org.acme.repository.*;
import java.util.List;

@ApplicationScoped
public class UsuarioService {

    @Inject UsuarioRepository usuarioRepo;
    @Inject RolRepository rolRepo;
    @Inject AlcaldiaRepository alcaldiaRepo;
    @Inject UsuarioAlcaldiaRepository usuarioAlcaldiaRepo;
    @Inject UsuarioMapper usuarioMapper;
    @Inject AlcaldiaMapper alcaldiaMapper;

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest req) {
        if (usuarioRepo.findByFirebaseUid(req.firebaseUid()).isPresent()) {
            throw AppException.conflict("Ya existe un usuario con ese firebase_uid");
        }
        Rol rol = rolRepo.findByIdOptional(req.idRol())
                .orElseThrow(() -> AppException.notFound("Rol no encontrado"));

        Usuario u = new Usuario();
        u.firebaseUid = req.firebaseUid();
        u.email = req.email();
        u.rol = rol;
        u.activo = true;
        usuarioRepo.persist(u);
        usuarioRepo.flush();

        return buildResponse(u);
    }

    public UsuarioResponse obtenerPorFirebaseUid(String uid) {
        Usuario u = usuarioRepo.findByFirebaseUid(uid)
                .orElseThrow(() -> AppException.notFound("Usuario no encontrado"));
        return buildResponse(u);
    }

    @Transactional
    public UsuarioResponse suscribirAlcaldia(Long idUsuario, SuscribirAlcaldiaRequest req) {
        Usuario u = usuarioRepo.findByIdOptional(idUsuario)
                .orElseThrow(() -> AppException.notFound("Usuario no encontrado"));
        Alcaldia alcaldia = alcaldiaRepo.findByIdOptional(req.idAlcaldia())
                .orElseThrow(() -> AppException.notFound("Alcaldía no encontrada"));

        if (usuarioAlcaldiaRepo.existeSuscripcion(idUsuario, req.idAlcaldia())) {
            throw AppException.conflict("El usuario ya está suscrito a esta alcaldía");
        }

        UsuarioAlcaldia ua = new UsuarioAlcaldia();
        ua.id = new UsuarioAlcaldiaId(idUsuario, req.idAlcaldia());
        ua.usuario = u;
        ua.alcaldia = alcaldia;
        usuarioAlcaldiaRepo.persist(ua);

        return buildResponse(u);
    }

    @Transactional
    public void desuscribirAlcaldia(Long idUsuario, Long idAlcaldia) {
        UsuarioAlcaldiaId pk = new UsuarioAlcaldiaId(idUsuario, idAlcaldia);
        UsuarioAlcaldia ua = usuarioAlcaldiaRepo.findByIdOptional(pk)
                .orElseThrow(() -> AppException.notFound("Suscripción no encontrada"));
        usuarioAlcaldiaRepo.delete(ua);
    }

    @Transactional
    public UsuarioResponse actualizarRol(Long idUsuario, ActualizarRolRequest req) {
        Usuario u = usuarioRepo.findByIdOptional(idUsuario)
                .orElseThrow(() -> AppException.notFound("Usuario no encontrado"));
        Rol rol = rolRepo.findByIdOptional(req.idRol())
                .orElseThrow(() -> AppException.notFound("Rol no encontrado"));
        u.rol = rol;
        return buildResponse(u);
    }

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepo.listAll().stream()
                .map(this::buildResponse)
                .toList();
    }


    private UsuarioResponse buildResponse(Usuario u) {
        List<AlcaldiaResumen> alcaldias = usuarioAlcaldiaRepo.findByUsuario(u.id).stream()
                .map(ua -> alcaldiaMapper.toResumen(ua.alcaldia))
                .toList();
        return usuarioMapper.toResponse(u, alcaldias);
    }
}
