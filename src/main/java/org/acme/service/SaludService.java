package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Alcaldia;
import org.acme.domain.entity.DatoSaludAgregado;
import org.acme.dto.response.SaludResponse;
import org.acme.exception.AppException;
import org.acme.repository.AlcaldiaRepository;
import org.acme.repository.DatoSaludAgregadoRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class SaludService {

    private static final Logger LOG = Logger.getLogger(SaludService.class);

    // INEGI código municipio CDMX (entero) → id_alcaldia en BD
    private static final Map<Integer, Long> MUNICIPIO_A_ALCALDIA = Map.ofEntries(
            Map.entry(2,  2L),   // Azcapotzalco
            Map.entry(3,  4L),   // Coyoacán
            Map.entry(4,  5L),   // Cuajimalpa
            Map.entry(5,  7L),   // Gustavo A. Madero
            Map.entry(6,  8L),   // Iztacalco
            Map.entry(7,  9L),   // Iztapalapa
            Map.entry(8,  10L),  // La Magdalena Contreras
            Map.entry(9,  12L),  // Milpa Alta
            Map.entry(10, 1L),   // Álvaro Obregón
            Map.entry(11, 13L),  // Tláhuac
            Map.entry(12, 14L),  // Tlalpan
            Map.entry(13, 16L),  // Xochimilco
            Map.entry(14, 3L),   // Benito Juárez
            Map.entry(15, 6L),   // Cuauhtémoc
            Map.entry(16, 11L),  // Miguel Hidalgo
            Map.entry(17, 15L)   // Venustiano Carranza
    );

    @ConfigProperty(name = "salud.csv.basepath", defaultValue = "docs/output_by_year")
    String csvBasePath;

    @Inject DatoSaludAgregadoRepository saludRepo;
    @Inject AlcaldiaRepository alcaldiaRepo;

    // ─── Importación ─────────────────────────────────────────────────────────────

    @Transactional
    public int importarDatos(int anio) {
        LOG.infof("[SALUD] Iniciando importación año %d desde %s", anio, csvBasePath);

        // Clave: (alcaldiaId, mes) → acumulador
        Map<String, BaseAccum> base    = new HashMap<>();
        Map<String, Integer>   epoc    = new HashMap<>();
        Map<String, Integer>   asma    = new HashMap<>();
        Map<String, Integer>   tabaq   = new HashMap<>();

        leerArchivoBase(anio, "PNEUMONIA", base);
        leerArchivoCasos(anio, "COPD",    epoc);
        leerArchivoCasos(anio, "ASTHMA",  asma);
        leerArchivoCasos(anio, "SMOKING", tabaq);

        if (base.isEmpty()) {
            throw AppException.badRequest("No se encontraron datos para el año " + anio
                    + ". Verifica que el archivo PNEUMONIA/" + anio + ".csv exista en " + csvBasePath);
        }

        saludRepo.deleteByAnio(anio);

        Map<Long, Alcaldia> alcaldias = new HashMap<>();
        alcaldiaRepo.listAll().forEach(a -> alcaldias.put(a.id, a));

        int persistidos = 0;
        for (Map.Entry<String, BaseAccum> entry : base.entrySet()) {
            String   clave      = entry.getKey();
            BaseAccum acum      = entry.getValue();
            long     alcaldiaId = Long.parseLong(clave.split(":")[0]);
            int      mes        = Integer.parseInt(clave.split(":")[1]);

            Alcaldia alcaldia = alcaldias.get(alcaldiaId);
            if (alcaldia == null) continue;

            DatoSaludAgregado dato = new DatoSaludAgregado();
            dato.alcaldia          = alcaldia;
            dato.anio              = anio;
            dato.mes               = mes;
            dato.totalCasos        = acum.totalCasos;
            dato.totalDefunciones  = acum.totalDefunciones;
            dato.casosNeumonia     = acum.casosEnfermedad;
            dato.casosEpoc         = epoc.getOrDefault(clave, 0);
            dato.casosAsma         = asma.getOrDefault(clave, 0);
            dato.casosTabaquismo   = tabaq.getOrDefault(clave, 0);
            dato.promedioEdad      = acum.countEdad > 0 ? acum.sumaEdad / acum.countEdad : null;

            saludRepo.persist(dato);
            persistidos++;
        }

        LOG.infof("[SALUD] Importación completada: %d registros para año %d", persistidos, anio);
        return persistidos;
    }

    // Lee PNEUMONIA (o cualquier archivo base) → totalCasos, defunciones, casosEnfermedad, edad
    private void leerArchivoBase(int anio, String carpeta, Map<String, BaseAccum> mapa) {
        Path archivo = resolverRuta(carpeta, anio);
        if (!Files.exists(archivo)) {
            LOG.warnf("[SALUD] Archivo no encontrado: %s", archivo);
            return;
        }
        LOG.infof("[SALUD] Leyendo base desde %s", archivo);

        try (BufferedReader br = Files.newBufferedReader(archivo)) {
            br.readLine(); // saltar encabezado
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] cols = linea.split(",", -1);
                if (cols.length < 6) continue;

                Integer munCode = parsearInt(cols[1]);
                if (munCode == null) continue;

                Long alcaldiaId = MUNICIPIO_A_ALCALDIA.get(munCode);
                if (alcaldiaId == null) continue; // fuera de CDMX

                int mes = parsearMes(cols[2]);
                if (mes < 1 || mes > 12) continue;

                String clave        = alcaldiaId + ":" + mes;
                boolean esMuerte    = !cols[3].isBlank();
                boolean tieneEnf    = "1".equals(cols[5].trim());
                Integer edad        = parsearInt(cols[4]);

                BaseAccum acum = mapa.computeIfAbsent(clave, k -> new BaseAccum());
                acum.totalCasos++;
                if (esMuerte) acum.totalDefunciones++;
                if (tieneEnf) acum.casosEnfermedad++;
                if (edad != null) { acum.sumaEdad += edad; acum.countEdad++; }
            }
        } catch (IOException e) {
            LOG.errorf("[SALUD] Error leyendo %s: %s", archivo, e.getMessage());
        }
    }

    // Lee COPD / ASTHMA / SMOKING → solo cuenta casos donde valor = 1
    private void leerArchivoCasos(int anio, String carpeta, Map<String, Integer> mapa) {
        Path archivo = resolverRuta(carpeta, anio);
        if (!Files.exists(archivo)) {
            LOG.warnf("[SALUD] Archivo no encontrado: %s", archivo);
            return;
        }
        LOG.infof("[SALUD] Leyendo casos desde %s", archivo);

        try (BufferedReader br = Files.newBufferedReader(archivo)) {
            br.readLine(); // saltar encabezado
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] cols = linea.split(",", -1);
                if (cols.length < 6) continue;
                if (!"1".equals(cols[5].trim())) continue; // solo casos positivos

                Integer munCode = parsearInt(cols[1]);
                if (munCode == null) continue;

                Long alcaldiaId = MUNICIPIO_A_ALCALDIA.get(munCode);
                if (alcaldiaId == null) continue;

                int mes = parsearMes(cols[2]);
                if (mes < 1 || mes > 12) continue;

                String clave = alcaldiaId + ":" + mes;
                mapa.merge(clave, 1, Integer::sum);
            }
        } catch (IOException e) {
            LOG.errorf("[SALUD] Error leyendo %s: %s", archivo, e.getMessage());
        }
    }

    // ─── Score de salud (0-100, mayor = más saludable) ───────────────────────────

    public double calcularScoreSalud(Long idAlcaldia) {
        Optional<DatoSaludAgregado> opt = saludRepo.findLatestByAlcaldia(idAlcaldia);
        if (opt.isEmpty()) {
            LOG.warnf("[IRSA] Salud: sin datos para alcaldía %d, usando neutro 50.0", idAlcaldia);
            return 50.0;
        }

        DatoSaludAgregado d = opt.get();
        if (d.totalCasos == 0) return 50.0;

        double mort   = (double) d.totalDefunciones / d.totalCasos;
        double neum   = (double) d.casosNeumonia    / d.totalCasos;
        double epocR  = (double) d.casosEpoc        / d.totalCasos;
        double asmaR  = (double) d.casosAsma        / d.totalCasos;
        double tabaq  = (double) d.casosTabaquismo  / d.totalCasos;

        // Penalización ponderada — umbrales: tasa donde se alcanza el 100% de penalización
        // Mortalidad 10% = techo, Neumonia 50% = techo, EPOC/Asma 30% = techo, Tabaquismo 50% = techo
        double penalizacion =
                Math.min(100, mort  * 1000) * 0.40 +
                Math.min(100, neum  *  200) * 0.25 +
                Math.min(100, epocR *  333) * 0.15 +
                Math.min(100, asmaR *  333) * 0.10 +
                Math.min(100, tabaq *  200) * 0.10;

        double score = Math.max(0, 100 - penalizacion);
        LOG.infof("[IRSA] Salud: alcaldía=%d | mort=%.3f | neum=%.3f | epoc=%.3f | asma=%.3f | tabaq=%.3f → score=%.2f",
                idAlcaldia, mort, neum, epocR, asmaR, tabaq, score);
        return score;
    }

    // ─── Consultas ───────────────────────────────────────────────────────────────

    public List<SaludResponse> listarPorAlcaldia(Long idAlcaldia) {
        if (!alcaldiaRepo.findByIdOptional(idAlcaldia).isPresent()) {
            throw AppException.notFound("Alcaldía no encontrada");
        }
        return saludRepo.findByAlcaldia(idAlcaldia).stream()
                .map(this::toResponse).toList();
    }

    public List<SaludResponse> listarPorAnio(int anio) {
        return saludRepo.findByAnio(anio).stream()
                .map(this::toResponse).toList();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private Path resolverRuta(String carpeta, int anio) {
        return Paths.get(csvBasePath, carpeta, anio + ".csv");
    }

    private Integer parsearInt(String valor) {
        try { return Integer.parseInt(valor.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    // Extrae mes de "2024-03-15 06:00:00+00:00" → 3
    private int parsearMes(String fechaStr) {
        try {
            String limpia = fechaStr.trim();
            if (limpia.length() < 7) return -1;
            return Integer.parseInt(limpia.substring(5, 7));
        } catch (Exception e) { return -1; }
    }

    private SaludResponse toResponse(DatoSaludAgregado d) {
        double total = d.totalCasos > 0 ? d.totalCasos : 1;
        return new SaludResponse(
                d.id,
                d.alcaldia.id,
                d.alcaldia.nombre,
                d.anio,
                d.mes,
                d.totalCasos,
                d.totalDefunciones,
                d.casosNeumonia,
                d.casosEpoc,
                d.casosAsma,
                d.casosTabaquismo,
                d.promedioEdad,
                d.totalDefunciones / total,
                d.casosNeumonia    / total,
                d.casosEpoc        / total,
                d.casosAsma        / total,
                d.casosTabaquismo  / total
        );
    }

    // Acumulador interno para el archivo base
    private static class BaseAccum {
        int    totalCasos;
        int    totalDefunciones;
        int    casosEnfermedad;
        double sumaEdad;
        int    countEdad;
    }
}
