package org.acme.service;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Alert;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.Role;
import org.acme.domain.entity.User;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.MunicipalitySummary;
import org.acme.repository.AlertRepository;
import org.acme.repository.MunicipalityRepository;
import org.acme.repository.RoleRepository;
import org.acme.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

@QuarkusTest
public class IrsaAlertSimulationTest {

    @Inject AlertEmailService alertEmailService;
    @Inject UserRepository userRepository;
    @Inject MunicipalityRepository municipalityRepository;
    @Inject AlertRepository alertRepository;
    @Inject RoleRepository roleRepository;
    @Inject MockMailbox mailbox;

    @Test
    @Transactional
    void simularDeteccionRiesgoYNotificacion() {
        // 0. Preparar Role
        Role role = new Role();
        role.setRoleName("CITIZEN_" + UUID.randomUUID());
        roleRepository.persist(role);

        // 1. Preparar datos de prueba
        String uniqueEmail = "vecino-" + UUID.randomUUID() + "@ciudad.mx";
        
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setNames("Vecino Vigilante");
        user.setFirebaseUid("test-uid-" + UUID.randomUUID());
        user.setRole(role);
        userRepository.persist(user);

        Municipality mun = new Municipality();
        mun.setMunicipalityName("Alcaldía Simulada " + UUID.randomUUID());
        municipalityRepository.persist(mun);

        // 2. Crear la suscripción (El usuario quiere alertas de esta alcaldía)
        Alert alert = new Alert();
        alert.setUser(user);
        alert.setMunicipality(mun);
        alert.setAlertType("IRSA_SUBSCRIPTION");
        alert.setMessage("Suscripción de prueba");
        alert.setIsActive(true);
        alertRepository.persist(alert);

        // 3. Simular que el motor IRSA detecta riesgo CRITICO
        IrsaResponse irsaAlto = new IrsaResponse(
                null,
                new MunicipalitySummary(mun.getId(), mun.getMunicipalityName()),
                0.95f,      // Valor IRSA muy alto
                "CRITICAL", // Nivel de riesgo
                false,
                null,
                Instant.now()
        );

        // 4. Ejecutar la lógica de notificación
        alertEmailService.sendRiskDetected(irsaAlto);

        // 5. VALIDACIÓN: ¿Le llegó el correo al suscriptor?
        // Esperamos un poco ya que el mailer es reactivo/asíncrono
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        var sentMessages = mailbox.getMessagesSentTo(uniqueEmail);
        Assertions.assertFalse(sentMessages.isEmpty(), "¡El suscriptor debería haber recibido un correo!");
        
        String subject = sentMessages.get(0).getSubject();
        System.out.println("\n--- SIMULACIÓN EXITOSA ---");
        System.out.println("Destinatario: " + uniqueEmail);
        System.out.println("Asunto: " + subject);
        System.out.println("Cuerpo: " + sentMessages.get(0).getText());
        System.out.println("--------------------------\n");
    }
}
