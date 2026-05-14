package org.acme.dto.response;

import java.time.LocalDate;

public record EpidemiologicoResponse(
        Long id,
        AlcaldiaResumen alcaldia,
        LocalDate fechaIngreso,
        Integer edad,
        Boolean epoc,
        Boolean asma,
        Boolean tabaquismo,
        Boolean resultadoPcr
) {}
