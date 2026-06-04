package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Alert;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.User;
import org.acme.dto.request.CreateAlertRequest;
import org.acme.dto.response.AlertResponse;
import org.acme.exception.AppException;
import org.acme.mapper.AlertMapper;
import org.acme.repository.AlertRepository;
import org.acme.repository.MunicipalityRepository;
import org.acme.repository.UserRepository;

import java.util.List;

@ApplicationScoped
public class AlertService {

    @Inject AlertRepository alertRepository;
    @Inject UserRepository userRepository;
    @Inject MunicipalityRepository municipalityRepository;
    @Inject AlertMapper alertMapper;
    @Inject AlertEmailService alertEmailService;

    @Transactional
    public AlertResponse create(Long userId, CreateAlertRequest request) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        Municipality municipality = municipalityRepository.findByIdOptional(request.municipalityId())
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        Alert alert = new Alert();
        alert.setUser(user);
        alert.setMunicipality(municipality);
        alert.setAlertType(request.alertType());
        alert.setMessage(request.message());
        alert.setIsActive(true);
        alertRepository.persist(alert);
        alertRepository.flush();
        alertEmailService.sendAlertCreated(alert);
        return alertMapper.toResponse(alert);
    }

    @Transactional
    public AlertResponse subscribe(Long userId, Long municipalityId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        Alert existing = alertRepository.findByUserAndMunicipality(userId, municipalityId).orElse(null);
        if (existing != null) {
            existing.setIsActive(true);
            alertEmailService.sendAlertCreated(existing);
            return alertMapper.toResponse(existing);
        }

        Alert alert = new Alert();
        alert.setUser(user);
        alert.setMunicipality(municipality);
        alert.setAlertType("SUBSCRIPTION");
        alert.setMessage("Subscribed to alerts for " + municipality.getMunicipalityName());
        alert.setIsActive(true);
        alertRepository.persist(alert);
        alertRepository.flush();
        alertEmailService.sendAlertCreated(alert);
        return alertMapper.toResponse(alert);
    }

    @Transactional
    public AlertResponse activate(Long alertId) {
        Alert alert = alertRepository.findByIdOptional(alertId)
                .orElseThrow(() -> AppException.notFound("Alert not found"));
        alert.setIsActive(true);
        return alertMapper.toResponse(alert);
    }

    @Transactional
    public void deactivate(Long alertId) {
        Alert alert = alertRepository.findByIdOptional(alertId)
                .orElseThrow(() -> AppException.notFound("Alert not found"));
        alert.setIsActive(false);
    }

    public List<AlertResponse> getByUser(Long userId) {
        return alertRepository.findByUser(userId).stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    public List<AlertResponse> getActiveByUser(Long userId) {
        return alertRepository.findActiveByUser(userId).stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    public List<AlertResponse> getByMunicipality(Long municipalityId) {
        return alertRepository.findActiveByMunicipality(municipalityId).stream()
                .map(alertMapper::toResponse)
                .toList();
    }
}
