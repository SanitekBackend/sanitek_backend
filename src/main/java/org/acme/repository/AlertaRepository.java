package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.entity.Alerta;
import org.acme.domain.enums.EstadoAlerta;
import java.util.List;

@ApplicationScoped
public class AlertaRepository implements PanacheRepository<Alerta> {

    public List<Alerta> findByUsuario(Long idUsuario) {
        return find("usuario.id = ?1",
                Sort.by("fechaAlerta").descending(),
                idUsuario).list();
    }

    public List<Alerta> findPendientesByAlcaldia(Long idAlcaldia) {
        return find("alcaldia.id = ?1 AND estado = ?2",
                Sort.by("fechaAlerta").descending(),
                idAlcaldia, EstadoAlerta.PENDIENTE).list();
    }

    public List<Alerta> findByUsuarioYEstado(Long idUsuario, EstadoAlerta estado) {
        return find("usuario.id = ?1 AND estado = ?2",
                Sort.by("fechaAlerta").descending(),
                idUsuario, estado).list();
    }

    public long contarPendientesByUsuario(Long idUsuario) {
        return count("usuario.id = ?1 AND estado = ?2", idUsuario, EstadoAlerta.PENDIENTE);
    }
}
