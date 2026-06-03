package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Company;
import org.acme.dto.request.CreateCompanyRequest;
import org.acme.dto.response.CompanyResponse;
import org.acme.exception.AppException;
import org.acme.mapper.CompanyMapper;
import org.acme.repository.CompanyRepository;

import java.util.List;

@ApplicationScoped
public class CompanyService {

    @Inject CompanyRepository companyRepository;
    @Inject CompanyMapper companyMapper;
    @Inject CurrentUserService currentUserService;

    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        currentUserService.requireSuperAdmin();
        String companyName = request.companyName().trim();
        if (companyRepository.findByCompanyName(companyName).isPresent()) {
            throw AppException.conflict("Company already exists");
        }

        Company company = new Company();
        company.setCompanyName(companyName);
        company.setIsActive(true);
        companyRepository.persist(company);
        return companyMapper.toResponse(company);
    }

    public List<CompanyResponse> listAll() {
        currentUserService.requireSuperAdmin();
        return companyRepository.listAll().stream()
                .map(companyMapper::toResponse)
                .toList();
    }
}
