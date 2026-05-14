package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Alcaldia;
import org.acme.domain.entity.DatoMeteorologico;
import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.MedicionContaminante;
import org.acme.domain.enums.NivelRiesgo;
import org.acme.domain.irsa.IrsaEngine;
import org.acme.domain.irsa.IrsaResult;
import org.acme.domain.irsa.IrsaWeightConfig;
import org.acme.dto.response.IrsaDiagnosticoResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.PuntoTendencia;
import org.acme.dto.response.TendenciaIrsaResponse;
import org.acme.exception.AppException;
import org.acme.mapper.IrsaMapper;
import org.acme.repository.AlcaldiaRepository;
import org.acme.repository.DatoMeteorologicoRepository;
import org.acme.repository.IrsaRepository;
import org.acme.repository.MedicionContaminanteRepository;
import org.acme.service.SaludService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
public class IrsaService {

    private static final Logger LOG = Logger.getLogger(IrsaService.class);

    @Inject IrsaRepository irsaRepo;
    @Inject AlcaldiaRepository alcaldiaRepo;
    @Inject IrsaMapper irsaMapper;
    @Inject MedicionContaminanteRepository medicionRepo;
    @Inject DatoMeteorologicoRepository meteorologicoRepo;
    @Inject SaludService saludService;

    public IrsaResponse obtenerUltimoPorAlcaldia(Long idAlcaldia) {
        return irsaRepo.findLatestByAlcaldia(idAlcaldia)
                .map(irsaMapper::toResponse)
                .orElseThrow(() -> AppException.notFound("No hay cálculo IRSA para esta alcaldía"));
    }

    public List<IrsaResponse> listarUltimos() {
        return irsaRepo.findAllLatest().stream()
                .map(irsaMapper::toResponse)
                .toList();
    }

    public List<IrsaResponse> obtenerHistorico(Long idAlcaldia, Instant desde, Instant hasta) {
        return irsaRepo.findHistoricoByAlcaldia(idAlcaldia, desde, hasta).stream()
                .map(irsaMapper::toResponse)
                .toList();
    }

    public List<IrsaResponse> listarPorNivelRiesgo(NivelRiesgo nivel) {
        return irsaRepo.findByNivelRiesgo(nivel).stream()
                .map(irsaMapper::toResponse)
                .toList();
    }

    @Transactional
    public IrsaResponse registrarCalculo(Long idAlcaldia, Float valorIrsa, String origen) {
        Alcaldia alcaldia = alcaldiaRepo.findByIdOptional(idAlcaldia)
                .orElseThrow(() -> AppException.notFound("Alcaldía no encontrada"));

        Irsa irsa = new Irsa();
        irsa.alcaldia = alcaldia;
        irsa.valorIrsa = valorIrsa;
        irsa.nivelRiesgo = calcularNivel(valorIrsa);
        irsa.origenCalculo = (origen != null && !origen.isBlank()) ? origen : "MANUAL";
        irsaRepo.persist(irsa);
        irsaRepo.flush();

        return irsaMapper.toResponse(irsa);
    }

    @Transactional
    public IrsaResponse calcularIrsa(Long idAlcaldia) {
        Alcaldia alcaldia = alcaldiaRepo.findByIdOptional(idAlcaldia)
                .orElseThrow(() -> AppException.notFound("Alcaldía no encontrada"));

        Instant hasta = Instant.now();
        Instant desde = hasta.minus(24, ChronoUnit.HOURS);

        double airScore    = calcularPuntajeAire(idAlcaldia, desde, hasta);
        double climateScore = calcularPuntajeClima(idAlcaldia);
        double socioScore   = calcularPuntajeSocioeconomico(alcaldia);
        double healthScore  = saludService.calcularScoreSalud(idAlcaldia);

        IrsaEngine engine = new IrsaEngine(IrsaWeightConfig.defaults());
        IrsaResult result = engine.calculate(airScore, climateScore, socioScore, healthScore);
        float valorIrsa = (float) Math.max(0.0, Math.min(1.0, 1.0 - result.score() / 100.0));

        LOG.infof("[IRSA] Alcaldía=%s | aire=%.2f | clima=%.2f | socio=%.2f | salud=%.2f | score=%.2f | valorIrsa=%.4f | nivel=%s",
                alcaldia.nombre, airScore, climateScore, socioScore, healthScore, result.score(), valorIrsa, calcularNivel(valorIrsa));

        Irsa irsa = new Irsa();
        irsa.alcaldia = alcaldia;
        irsa.valorIrsa = valorIrsa;
        irsa.nivelRiesgo = calcularNivel(valorIrsa);
        irsa.origenCalculo = "MANUAL";
        irsaRepo.persist(irsa);
        irsaRepo.flush();

        return irsaMapper.toResponse(irsa);
    }

    public IrsaDiagnosticoResponse diagnosticar(Long idAlcaldia) {
        Alcaldia alcaldia = alcaldiaRepo.findByIdOptional(idAlcaldia)
                .orElseThrow(() -> AppException.notFound("Alcaldía no encontrada"));

        Instant hasta = Instant.now();
        Instant desde = hasta.minus(24, ChronoUnit.HOURS);

        List<MedicionContaminante> mediciones = medicionRepo.findByAlcaldiaAndFechaRange(idAlcaldia, desde, hasta);
        Map<String, Double> promedios = mediciones.stream()
                .filter(m -> m.valorMedicion != null && m.contaminante != null)
                .collect(Collectors.groupingBy(
                        m -> m.contaminante.nomenclatura,
                        Collectors.averagingDouble(m -> m.valorMedicion)
                ));

        List<DatoMeteorologico> datosClima = meteorologicoRepo.findByAlcaldia(idAlcaldia);

        double airScore    = calcularPuntajeAire(idAlcaldia, desde, hasta);
        double climateScore = calcularPuntajeClima(idAlcaldia);
        double socioScore   = calcularPuntajeSocioeconomico(alcaldia);
        double healthScore  = saludService.calcularScoreSalud(idAlcaldia);

        IrsaEngine engine = new IrsaEngine(IrsaWeightConfig.defaults());
        IrsaResult result = engine.calculate(airScore, climateScore, socioScore, healthScore);
        double valorIrsa = Math.max(0.0, Math.min(1.0, 1.0 - result.score() / 100.0));

        LOG.infof("[IRSA] Diagnóstico %s | aire=%.2f×0.35 | clima=%.2f×0.25 | socio=%.2f×0.20 | salud=%.2f×0.20 → score=%.2f → valorIrsa=%.4f | nivel=%s",
                alcaldia.nombre, airScore, climateScore, socioScore, healthScore,
                result.score(), valorIrsa, calcularNivel((float) valorIrsa));

        Double temperatura = null;
        Double humedad = null;
        if (!datosClima.isEmpty()) {
            DatoMeteorologico datoActual = datosClima.get(0);
            temperatura = datoActual.temperaturaAmbiental != null ? datoActual.temperaturaAmbiental.doubleValue() : null;
            humedad = datoActual.humedadRelativa != null ? datoActual.humedadRelativa.doubleValue() : null;
        }

        return new IrsaDiagnosticoResponse(
                idAlcaldia,
                alcaldia.nombre,
                airScore,
                climateScore,
                socioScore,
                healthScore,
                result.score(),
                valorIrsa,
                calcularNivel((float) valorIrsa).name(),
                mediciones.size(),
                promedios,
                !datosClima.isEmpty(),
                alcaldia.nivelRezago != null ? alcaldia.nivelRezago.name() : "N/D",
                temperatura,
                humedad
        );
    }

    public TendenciaIrsaResponse obtenerTendencia(Long idAlcaldia, String periodo, int cantidad) {
        Alcaldia alcaldia = alcaldiaRepo.findByIdOptional(idAlcaldia)
                .orElseThrow(() -> AppException.notFound("Alcaldía no encontrada"));

        boolean esMensual = "MENSUAL".equalsIgnoreCase(periodo);
        Instant hasta = Instant.now();
        Instant desde = esMensual
                ? hasta.minus(cantidad * 30L, ChronoUnit.DAYS)
                : hasta.minus(cantidad * 7L,  ChronoUnit.DAYS);

        List<Irsa> registros = irsaRepo.findHistoricoByAlcaldia(idAlcaldia, desde, hasta);

        if (registros.isEmpty()) {
            return new TendenciaIrsaResponse(idAlcaldia, alcaldia.nombre,
                    periodo.toUpperCase(), cantidad, "SIN_DATOS", 0.0, List.of());
        }

        ZoneId cdmx = ZoneId.of("America/Mexico_City");

        // Agrupar por clave ordenable ("2025-04" mensual, "2025-W18" semanal)
        Map<String, List<Irsa>> agrupados = new TreeMap<>(registros.stream()
                .collect(Collectors.groupingBy(i -> {
                    ZonedDateTime zdt = i.fechaCalculo.atZone(cdmx);
                    if (esMensual) {
                        return String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
                    } else {
                        int semana = zdt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                        int anio   = zdt.get(IsoFields.WEEK_BASED_YEAR);
                        return String.format("%04d-W%02d", anio, semana);
                    }
                })));

        String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};

        List<PuntoTendencia> puntos = agrupados.entrySet().stream()
                .map(e -> {
                    List<Double> valores = e.getValue().stream()
                            .map(i -> (double) i.valorIrsa).toList();
                    double prom = valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double min  = valores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                    double max  = valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);

                    // Etiqueta legible a partir de la clave
                    String clave = e.getKey();
                    String etiqueta;
                    if (esMensual) {
                        int anio = Integer.parseInt(clave.substring(0, 4));
                        int mes  = Integer.parseInt(clave.substring(5, 7));
                        etiqueta = meses[mes - 1] + " " + anio;
                    } else {
                        etiqueta = "Sem " + clave.substring(6) + " (" + clave.substring(0, 4) + ")";
                    }

                    return new PuntoTendencia(etiqueta, prom, min, max,
                            calcularNivel((float) prom).name(), valores.size());
                })
                .toList();

        double variacion = 0.0;
        String tendencia = "ESTABLE";
        if (puntos.size() >= 2) {
            variacion = puntos.getLast().promedioIrsa() - puntos.getFirst().promedioIrsa();
            if      (variacion >  0.05) tendencia = "EMPEORANDO";
            else if (variacion < -0.05) tendencia = "MEJORANDO";
        }

        return new TendenciaIrsaResponse(idAlcaldia, alcaldia.nombre,
                periodo.toUpperCase(), cantidad, tendencia, variacion, puntos);
    }

    // ─── Snapshot diario ─────────────────────────────────────────────────────────

    public List<IrsaResponse> obtenerSnapshotDiario(LocalDate fecha) {
        ZoneId cdmx   = ZoneId.of("America/Mexico_City");
        Instant inicio = fecha.atStartOfDay(cdmx).toInstant();
        Instant fin    = fecha.plusDays(1).atStartOfDay(cdmx).toInstant();

        List<Irsa> registros;
        if (!fecha.isAfter(LocalDate.now(cdmx))) {
            // Fecha histórica o hoy — busca registros reales
            registros = irsaRepo.findHistoricoByRango(inicio, fin);
        } else {
            // Fecha futura — busca predicciones
            registros = irsaRepo.findPrediccionesByRango(inicio, fin);
        }

        // Para cada alcaldía toma solo el registro más reciente del día
        return registros.stream()
                .collect(Collectors.toMap(
                        i -> i.alcaldia.id,
                        i -> i,
                        (a, b) -> a.fechaCalculo.isAfter(b.fechaCalculo) ? a : b
                ))
                .values().stream()
                .sorted(Comparator.comparing(i -> i.alcaldia.id))
                .map(irsaMapper::toResponse)
                .toList();
    }

    // ─── Generación de predicciones ──────────────────────────────────────────────

    @Transactional
    public void generarPredicciones(int diasAdelante) {
        ZoneId cdmx    = ZoneId.of("America/Mexico_City");
        LocalDate hoy  = LocalDate.now(cdmx);
        Instant desde7 = hoy.minusDays(7).atStartOfDay(cdmx).toInstant();
        Instant ahora  = Instant.now();

        // Borrar predicciones existentes en el rango para no duplicar
        Instant inicioRango = hoy.plusDays(1).atStartOfDay(cdmx).toInstant();
        Instant finRango    = hoy.plusDays(diasAdelante + 1).atStartOfDay(cdmx).toInstant();
        long eliminadas = irsaRepo.deletePredicciones(inicioRango, finRango);
        LOG.infof("[PREDICCION] Eliminadas %d predicciones previas", eliminadas);

        List<Alcaldia> alcaldias = alcaldiaRepo.listAll();
        int generadas = 0;

        for (Alcaldia alcaldia : alcaldias) {
            List<Irsa> historico = irsaRepo.findHistoricoNoPrediccion(alcaldia.id, desde7, ahora);
            if (historico.isEmpty()) {
                LOG.warnf("[PREDICCION] Sin histórico para alcaldía %s, omitiendo", alcaldia.nombre);
                continue;
            }

            float promedioIrsa = (float) historico.stream()
                    .mapToDouble(i -> i.valorIrsa)
                    .average()
                    .orElse(0.5);

            for (int d = 1; d <= diasAdelante; d++) {
                LocalDate fechaTarget = hoy.plusDays(d);

                Irsa prediccion = new Irsa();
                prediccion.alcaldia        = alcaldia;
                prediccion.valorIrsa       = promedioIrsa;
                prediccion.nivelRiesgo     = calcularNivel(promedioIrsa);
                prediccion.origenCalculo   = "PREDICCION";
                prediccion.fechaCalculo    = ahora;
                prediccion.fechaPrediccion = fechaTarget.atStartOfDay(cdmx).toInstant();
                irsaRepo.persist(prediccion);
                generadas++;
            }
        }
        LOG.infof("[PREDICCION] Generadas %d predicciones para los próximos %d días", generadas, diasAdelante);
    }

    // Umbrales definidos en la arquitectura v2.0
    private NivelRiesgo calcularNivel(float valor) {
        if (valor <= 0.25f) return NivelRiesgo.BAJO;
        if (valor <= 0.50f) return NivelRiesgo.MODERADO;
        if (valor <= 0.75f) return NivelRiesgo.ALTO;
        return NivelRiesgo.CRITICO;
    }

    private double calcularPuntajeAire(Long idAlcaldia, Instant desde, Instant hasta) {
        List<MedicionContaminante> mediciones = medicionRepo.findByAlcaldiaAndFechaRange(idAlcaldia, desde, hasta);
        if (mediciones.isEmpty()) {
            LOG.warnf("[IRSA] Aire: sin mediciones en las últimas 24h para alcaldía %d, usando neutro 50.0", idAlcaldia);
            return 50.0;
        }

        Map<String, Double> promedios = mediciones.stream()
                .filter(m -> m.valorMedicion != null && m.contaminante != null)
                .collect(Collectors.groupingBy(
                        m -> m.contaminante.nomenclatura,
                        Collectors.averagingDouble(m -> m.valorMedicion)
                ));

        LOG.infof("[IRSA] Aire: %d mediciones encontradas, promedios por contaminante: %s", mediciones.size(), promedios);

        List<Double> scores = new ArrayList<>();

        // Umbrales NOM-SEMARNAT / IMECA (µg/m³): valor donde puntaje de calidad = 0
        if (promedios.containsKey("NO2"))
            scores.add(Math.max(0, 100 - promedios.get("NO2") / 2.1));   // límite 210 µg/m³
        if (promedios.containsKey("O3"))
            scores.add(Math.max(0, 100 - promedios.get("O3") / 1.4));    // límite 140 µg/m³
        if (promedios.containsKey("PM2.5"))
            scores.add(Math.max(0, 100 - promedios.get("PM2.5") / 0.45)); // límite 45 µg/m³

        double resultado = scores.isEmpty() ? 50.0 : scores.stream().mapToDouble(Double::doubleValue).average().orElse(50.0);
        LOG.infof("[IRSA] Aire: puntaje final = %.2f", resultado);
        return resultado;
    }

    private double calcularPuntajeClima(Long idAlcaldia) {
        List<DatoMeteorologico> datos = meteorologicoRepo.findByAlcaldia(idAlcaldia);
        if (datos.isEmpty()) {
            LOG.warnf("[IRSA] Clima: sin datos meteorológicos para alcaldía %d, usando neutro 50.0", idAlcaldia);
            return 50.0;
        }

        DatoMeteorologico dato = datos.get(0);
        LOG.infof("[IRSA] Clima: temperatura=%.1f°C, humedad=%.1f%%", dato.temperaturaAmbiental, dato.humedadRelativa);

        List<Double> scores = new ArrayList<>();

        if (dato.temperaturaAmbiental != null) {
            // Óptimo CDMX: 16-22°C, penalización de 5 puntos por grado de desviación respecto a 19
            double tempScore = 100 - Math.min(100, Math.abs(dato.temperaturaAmbiental - 19.0) * 5);
            scores.add(Math.max(0, tempScore));
        }
        if (dato.humedadRelativa != null) {
            // Óptimo: 40-60%, penalización de 2 puntos por punto porcentual de desviación respecto a 50
            double humScore = 100 - Math.min(100, Math.abs(dato.humedadRelativa - 50.0) * 2);
            scores.add(Math.max(0, humScore));
        }

        double resultado = scores.isEmpty() ? 50.0 : scores.stream().mapToDouble(Double::doubleValue).average().orElse(50.0);
        LOG.infof("[IRSA] Clima: puntaje final = %.2f", resultado);
        return resultado;
    }

    private double calcularPuntajeSocioeconomico(Alcaldia alcaldia) {
        if (alcaldia.nivelRezago == null) {
            LOG.warnf("[IRSA] Socio: alcaldía '%s' sin nivelRezago, usando neutro 50.0", alcaldia.nombre);
            return 50.0;
        }
        double score = switch (alcaldia.nivelRezago) {
            case BAJO -> 80.0;
            case MEDIO -> 55.0;
            case ALTO -> 30.0;
            case MUY_ALTO -> 10.0;
        };
        LOG.infof("[IRSA] Socio: alcaldía '%s', nivelRezago=%s, puntaje=%.1f", alcaldia.nombre, alcaldia.nivelRezago, score);
        return score;
    }
}
