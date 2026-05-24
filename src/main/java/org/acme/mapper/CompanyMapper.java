package org.acme.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Company;
import org.acme.dto.response.CompanyResponse;
import org.acme.dto.response.CompanySummary;

@ApplicationScoped
public class CompanyMapper {

    public CompanyResponse toResponse(Company company) {
        return new CompanyResponse(company.getId(), company.getCompanyName(), company.getIsActive());
    }

    public CompanySummary toSummary(Company company) {
        return new CompanySummary(company.getId(), company.getCompanyName());
    }
}
