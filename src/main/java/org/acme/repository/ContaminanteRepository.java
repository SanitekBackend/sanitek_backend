package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.ContaminanteEntity;
import org.acme.domain.enums.PollutantType;

import java.util.List;

@ApplicationScoped
public class ContaminanteRepository implements PanacheRepository<ContaminanteEntity> {

    public List<ContaminanteEntity> findByMunicipioAndAnio(String municipio, Integer anio) {
        return list("municipio = ?1 and anio = ?2", municipio, anio);
    }

    public List<ContaminanteEntity> findByAnio(Integer anio) {
        return list("anio", anio);
    }

    public List<ContaminanteEntity> findByTipoContAndAnio(PollutantType tipoCont, Integer anio) {
        return list("tipoCont = ?1 and anio = ?2", tipoCont, anio);
    }
}
