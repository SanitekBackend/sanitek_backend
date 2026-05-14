package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.*;
import org.acme.domain.enums.EstadoAlerta;
import org.acme.dto.request.CrearAlertaRequest;
import org.acme.dto.response.AlertaResponse;
import org.acme.exception.AppException;
import org.acme.mapper.AlertaMapper;
import org.acme.repository.*;
import java.util.List;

@ApplicationScoped
public class AlertaService {

    @Inject AlertaRepository alertaRepo;
    @Inject UsuarioRepository usuarioRepo;
    @Inject AlcaldiaRepository alcaldiaRepo;
    @Inject AlertaMapper alertaMapper;

    @Transactional
    public AlertaResponse crearAlerta(Long idUsuario, CrearAlertaRequest req) {
        Usuario usuario = usuarioRepo.findByIdOptional(idUsuario)
                .orElseThrow(() -> AppException.notFound("Usuario no encontrado"));
        Alcaldia alcaldia = alcaldiaRepo.findByIdOptional(req.idAlcaldia())
                .orElseThrow(() -> AppException.notFound("Alcaldía no encontrada"));

        Alerta alerta = new Alerta();
        alerta.usuario = usuario;
        alerta.alcaldia = alcaldia;
        alerta.tipoAlerta = req.tipoAlerta();
        alerta.mensaje = req.mensaje();
        alerta.estado = EstadoAlerta.PENDIENTE;
        alertaRepo.persist(alerta);
        alertaRepo.flush();

        return alertaMapper.toResponse(alerta);
    }

    public List<AlertaResponse> obtenerPorUsuario(Long idUsuario) {
        return alertaRepo.findByUsuario(idUsuario).stream()
                .map(alertaMapper::toResponse)
                .toList();
    }

    @Transactional
    public AlertaResponse marcarLeida(Long idAlerta, Long idUsuario) {
        Alerta alerta = alertaRepo.findByIdOptional(idAlerta)
                .orElseThrow(() -> AppException.notFound("Alerta no encontrada"));

        // Verificar propiedad antes de modificar el estado
        if (!alerta.usuario.id.equals(idUsuario)) {
            throw AppException.forbidden("Esta alerta no pertenece al usuario");
        }

        alerta.estado = EstadoAlerta.LEIDA;
        return alertaMapper.toResponse(alerta);
    }

    public List<AlertaResponse> obtenerPendientesPorAlcaldia(Long idAlcaldia) {
        return alertaRepo.findPendientesByAlcaldia(idAlcaldia).stream()
                .map(alertaMapper::toResponse)
                .toList();
    }
}
