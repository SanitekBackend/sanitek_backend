package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Company;

import java.util.Optional;

@ApplicationScoped
public class CompanyRepository implements PanacheRepository<Company> {

    public Optional<Company> findByCompanyName(String companyName) {
        return find("companyName", companyName).firstResultOptional();
    }
}
