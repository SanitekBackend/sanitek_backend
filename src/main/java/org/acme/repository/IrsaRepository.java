package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Irsa;
import org.acme.domain.enums.NivelRiesgo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class IrsaRepository implements PanacheRepository<Irsa> {

    public Optional<Irsa> findLatestByAlcaldia(Long idAlcaldia) {
        return find("alcaldia.id = ?1",
                Sort.by("fechaCalculo").descending(),
                idAlcaldia).firstResultOptional();
    }

    public List<Irsa> findByNivelRiesgo(NivelRiesgo nivel) {
        return find("nivelRiesgo", nivel).list();
    }

    public List<Irsa> findHistoricoByAlcaldia(Long idAlcaldia, Instant desde, Instant hasta) {
        return find("alcaldia.id = ?1 AND fechaCalculo >= ?2 AND fechaCalculo <= ?3",
                Sort.by("fechaCalculo").descending(),
                idAlcaldia, desde, hasta).list();
    }

    public List<Irsa> findAllLatest() {
        return getEntityManager()
                .createQuery("""
                        SELECT i FROM Irsa i
                        WHERE i.origenCalculo <> 'PREDICCION'
                          AND i.fechaCalculo = (
                              SELECT MAX(i2.fechaCalculo) FROM Irsa i2
                              WHERE i2.alcaldia = i.alcaldia
                                AND i2.origenCalculo <> 'PREDICCION'
                          )
                        """, Irsa.class)
                .getResultList();
    }

    // Todos los registros históricos (no predicción) para un rango de tiempo (un día completo)
    public List<Irsa> findHistoricoByRango(Instant inicio, Instant fin) {
        return list("fechaCalculo >= ?1 AND fechaCalculo < ?2 AND origenCalculo <> 'PREDICCION'",
                Sort.by("fechaCalculo").descending(), inicio, fin);
    }

    // Todos los registros de predicción cuya fecha objetivo cae en el rango dado
    public List<Irsa> findPrediccionesByRango(Instant inicio, Instant fin) {
        return list("fechaPrediccion >= ?1 AND fechaPrediccion < ?2 AND origenCalculo = 'PREDICCION'",
                Sort.by("fechaCalculo").descending(), inicio, fin);
    }

    // Últimos N días de IRSA real por alcaldía (para calcular promedios de predicción)
    public List<Irsa> findHistoricoNoPrediccion(Long idAlcaldia, Instant desde, Instant hasta) {
        return list("alcaldia.id = ?1 AND fechaCalculo >= ?2 AND fechaCalculo <= ?3 AND origenCalculo <> 'PREDICCION'",
                Sort.by("fechaCalculo").descending(), idAlcaldia, desde, hasta);
    }

    // Elimina predicciones existentes de todas las alcaldías en un rango, para regenerarlas
    @Transactional
    public long deletePredicciones(Instant desde, Instant fin) {
        return delete("origenCalculo = 'PREDICCION' AND fechaPrediccion >= ?1 AND fechaPrediccion < ?2",
                desde, fin);
    }
}
