package org.acme.dto.response;

import java.util.List;

public record TendenciaIrsaResponse( // dto tendencia IRSA
        Long idAlcaldia,
        String nombreAlcaldia,
        String periodo,            
        int cantidadPeriodos,
        String tendencia,          
        double variacion,          
        List<PuntoTendencia> puntos
) {}
