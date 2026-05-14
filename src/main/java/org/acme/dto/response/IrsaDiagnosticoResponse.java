package org.acme.dto.response;

import java.util.List;
import java.util.Map;

public record IrsaDiagnosticoResponse(
        Long idAlcaldia,
        String nombreAlcaldia,
        double puntajeAire,
        double puntajeClima,
        double puntajeSocioeconomico,
        double puntajeSalud,
        double scoreMotor,
        double valorIrsa,
        String nivelRiesgo,
        int medicionesAireEncontradas,
        Map<String, Double> promediosPorContaminante,
        boolean tieneDataClima,
        String nivelRezagoSocial,
        Double temperatura,
        Double humedad
) {}
