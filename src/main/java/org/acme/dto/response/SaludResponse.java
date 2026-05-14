package org.acme.dto.response;

public record SaludResponse(
        Long id,
        Long idAlcaldia,
        String nombreAlcaldia,
        Integer anio,
        Integer mes,
        Integer totalCasos,
        Integer totalDefunciones,
        Integer casosNeumonia,
        Integer casosEpoc,
        Integer casosAsma,
        Integer casosTabaquismo,
        Double promedioEdad,
        Double tasaMortalidad,
        Double tasaNeumonia,
        Double tasaEpoc,
        Double tasaAsma,
        Double tasaTabaquismo
) {}
