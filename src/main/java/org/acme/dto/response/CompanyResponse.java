package org.acme.dto.response;

public record CompanyResponse(
        Long id,
        String companyName,
        Boolean isActive
) {}
