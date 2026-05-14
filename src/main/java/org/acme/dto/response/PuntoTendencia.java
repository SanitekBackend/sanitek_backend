package org.acme.dto.response;

public record PuntoTendencia(
        String etiqueta,       //fecha 
        double promedioIrsa,   // promedio del período (0-1)
        double minIrsa,
        double maxIrsa,
        String nivelPromedio,  // nivel de riesgo del promedio
        int cantidad           
) {}
