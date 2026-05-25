package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.NO2;
import org.acme.domain.entity.O3;
import org.acme.domain.entity.PM25;
import org.acme.domain.entity.Temperature;
import org.acme.domain.irsa.IrsaEngine;
import org.acme.domain.irsa.IrsaResult;
import org.acme.domain.irsa.IrsaWeightConfig;
import org.acme.dto.response.IrsaDiagnosticResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaTrendResponse;
import org.acme.dto.response.TrendPoint;
import org.acme.exception.AppException;
import org.acme.mapper.IrsaMapper;
import org.acme.repository.IrsaRepository;
import org.acme.repository.MunicipalityRepository;
import org.acme.repository.NO2Repository;
import org.acme.repository.O3Repository;
import org.acme.repository.PM25Repository;
import org.acme.repository.StationRepository;
import org.acme.repository.TemperatureRepository;
import org.jboss.logging.Logger;

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

@ApplicationScoped
public class IrsaService {

    private static final Logger LOG = Logger.getLogger(IrsaService.class);

    @Inject IrsaRepository irsaRepository;
    @Inject MunicipalityRepository municipalityRepository;
    @Inject StationRepository stationRepository;
    @Inject NO2Repository no2Repository;
    @Inject O3Repository o3Repository;
    @Inject PM25Repository pm25Repository;
    @Inject TemperatureRepository temperatureRepository;
    @Inject HealthService healthService;
    @Inject IrsaMapper irsaMapper;

    public IrsaResponse getLatestByMunicipality(Long municipalityId) {
        return irsaRepository.findLatestByMunicipality(municipalityId)
                .map(irsaMapper::toResponse)
                .orElseThrow(() -> AppException.notFound("No IRSA calculation found for this municipality"));
    }

    public List<IrsaResponse> listLatestAll() {
        return irsaRepository.findAllLatest().stream()
                .map(irsaMapper::toResponse)
                .toList();
    }

    public List<IrsaResponse> getHistorical(Long municipalityId, Instant from, Instant to) {
        return irsaRepository.findHistoricalByMunicipality(municipalityId, from, to).stream()
                .map(irsaMapper::toResponse)
                .toList();
    }

    public List<IrsaResponse> listByRiskLevel(String riskLevel) {
        return irsaRepository.findByRiskLevel(riskLevel).stream()
                .map(irsaMapper::toResponse)
                .toList();
    }

    @Transactional
    public IrsaResponse calculate(Long municipalityId) {
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        Instant to   = Instant.now();
        Instant from = to.minus(24, ChronoUnit.HOURS);

        List<Long> stationIds = stationRepository.findIdsByMunicipality(municipalityId);

        double airScore    = calculateAirScore(stationIds, from, to);
        double climateScore = calculateClimateScore(stationIds);
        double socioScore  = calculateSocioScore(municipality);
        double healthScore = healthService.calculateHealthScore(municipalityId);

        IrsaEngine engine = new IrsaEngine(IrsaWeightConfig.defaults());
        IrsaResult result = engine.calculate(airScore, climateScore, socioScore, healthScore);
        float irsaValue   = (float) Math.max(0.0, Math.min(1.0, 1.0 - result.score() / 100.0));

        LOG.infof("[IRSA] Municipality=%s | air=%.2f | climate=%.2f | socio=%.2f | health=%.2f | score=%.2f | irsaValue=%.4f | level=%s",
                municipality.getMunicipalityName(), airScore, climateScore, socioScore, healthScore,
                result.score(), irsaValue, calculateLevel(irsaValue));

        Irsa irsa = new Irsa();
        irsa.setMunicipality(municipality);
        irsa.setIrsaValue(irsaValue);
        irsa.setRiskLevel(calculateLevel(irsaValue));
        irsa.setIsForecast(false);
        irsaRepository.persist(irsa);

        return irsaMapper.toResponse(irsa);
    }

    public IrsaDiagnosticResponse getDiagnostic(Long municipalityId) {
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        Instant to   = Instant.now();
        Instant from = to.minus(24, ChronoUnit.HOURS);

        List<Long> stationIds = stationRepository.findIdsByMunicipality(municipalityId);

        List<NO2>   no2List   = no2Repository.findByStationsAndDateRange(stationIds, from, to);
        List<O3>    o3List    = o3Repository.findByStationsAndDateRange(stationIds, from, to);
        List<PM25> pm25List = pm25Repository.findByStationsAndDateRange(stationIds, from, to);
        List<Temperature> temps = temperatureRepository.findLatestByStations(stationIds);

        Map<String, Double> avgByPollutant = new java.util.LinkedHashMap<>();
        computeAverage(no2List.stream().map(NO2::getMetricValue).toList())
                .ifPresent(v -> avgByPollutant.put("NO2", v));
        computeAverage(o3List.stream().map(O3::getMetricValue).toList())
                .ifPresent(v -> avgByPollutant.put("O3", v));
        computeAverage(pm25List.stream().map(PM25::getMetricValue).toList())
                .ifPresent(v -> avgByPollutant.put("PM2.5", v));

        double airScore    = calculateAirScore(stationIds, from, to);
        double climateScore = calculateClimateScore(stationIds);
        double socioScore  = calculateSocioScore(municipality);
        double healthScore = healthService.calculateHealthScore(municipalityId);

        IrsaEngine engine = new IrsaEngine(IrsaWeightConfig.defaults());
        IrsaResult result = engine.calculate(airScore, climateScore, socioScore, healthScore);
        double irsaValue  = Math.max(0.0, Math.min(1.0, 1.0 - result.score() / 100.0));

        return new IrsaDiagnosticResponse(
                municipalityId,
                municipality.getMunicipalityName(),
                airScore, climateScore, socioScore, healthScore,
                result.score(), irsaValue,
                calculateLevel((float) irsaValue),
                no2List.size(), o3List.size(), pm25List.size(),
                avgByPollutant,
                !temps.isEmpty(),
                municipality.getSocialVulnerability()
        );
    }

    public IrsaTrendResponse getTrend(Long municipalityId, String period, int count) {
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        boolean monthly = "MONTHLY".equalsIgnoreCase(period);
        Instant to   = Instant.now();
        Instant from = monthly
                ? to.minus(count * 30L, ChronoUnit.DAYS)
                : to.minus(count * 7L,  ChronoUnit.DAYS);

        List<Irsa> records = irsaRepository.findHistoricalByMunicipality(municipalityId, from, to);

        if (records.isEmpty()) {
            return new IrsaTrendResponse(municipalityId, municipality.getMunicipalityName(),
                    period.toUpperCase(), count, "NO_DATA", 0.0, List.of());
        }

        ZoneId cdmx = ZoneId.of("America/Mexico_City");
        Map<String, List<Irsa>> grouped = new TreeMap<>(records.stream()
                .collect(Collectors.groupingBy(i -> {
                    ZonedDateTime zdt = i.getCreatedAt().atZone(cdmx);
                    if (monthly) {
                        return String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
                    } else {
                        int week = zdt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                        int year = zdt.get(IsoFields.WEEK_BASED_YEAR);
                        return String.format("%04d-W%02d", year, week);
                    }
                })));

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        List<TrendPoint> points = grouped.entrySet().stream()
                .map(e -> {
                    List<Double> values = e.getValue().stream()
                            .map(i -> (double) i.getIrsaValue()).toList();
                    double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                    double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);

                    String key   = e.getKey();
                    String label;
                    if (monthly) {
                        int yr = Integer.parseInt(key.substring(0, 4));
                        int mo = Integer.parseInt(key.substring(5, 7));
                        label  = months[mo - 1] + " " + yr;
                    } else {
                        label = "W" + key.substring(6) + " (" + key.substring(0, 4) + ")";
                    }

                    return new TrendPoint(label, avg, min, max,
                            calculateLevel((float) avg), values.size());
                })
                .toList();

        double variation = 0.0;
        String trend     = "STABLE";
        if (points.size() >= 2) {
            variation = points.getLast().avgIrsa() - points.getFirst().avgIrsa();
            if      (variation >  0.05) trend = "WORSENING";
            else if (variation < -0.05) trend = "IMPROVING";
        }

        return new IrsaTrendResponse(municipalityId, municipality.getMunicipalityName(),
                period.toUpperCase(), count, trend, variation, points);
    }

    public List<IrsaResponse> getDailySnapshot(LocalDate date) {
        ZoneId cdmx = ZoneId.of("America/Mexico_City");
        Instant start = date.atStartOfDay(cdmx).toInstant();
        Instant end   = date.plusDays(1).atStartOfDay(cdmx).toInstant();

        List<Irsa> records;
        if (!date.isAfter(LocalDate.now(cdmx))) {
            records = irsaRepository.findHistoricalByRange(start, end);
        } else {
            records = irsaRepository.findForecastsByRange(start, end);
        }

        return records.stream()
                .collect(Collectors.toMap(
                        i -> i.getMunicipality().getId(),
                        i -> i,
                        (a, b) -> a.getCreatedAt().isAfter(b.getCreatedAt()) ? a : b
                ))
                .values().stream()
                .sorted(Comparator.comparing(i -> i.getMunicipality().getId()))
                .map(irsaMapper::toResponse)
                .toList();
    }

    @Transactional
    public void generateForecasts(int daysAhead) {
        ZoneId cdmx   = ZoneId.of("America/Mexico_City");
        LocalDate today = LocalDate.now(cdmx);
        Instant since7  = today.minusDays(7).atStartOfDay(cdmx).toInstant();
        Instant now     = Instant.now();

        Instant rangeStart = today.plusDays(1).atStartOfDay(cdmx).toInstant();
        Instant rangeEnd   = today.plusDays(daysAhead + 1).atStartOfDay(cdmx).toInstant();
        long deleted = irsaRepository.deleteForecasts(rangeStart, rangeEnd);
        LOG.infof("[FORECAST] Deleted %d existing forecasts", deleted);

        List<Municipality> municipalities = municipalityRepository.listAll();
        int generated = 0;

        for (Municipality municipality : municipalities) {
            List<Irsa> history = irsaRepository.findHistoricalNoForecast(municipality.getId(), since7, now);
            if (history.isEmpty()) {
                LOG.warnf("[FORECAST] No history for municipality %s, skipping", municipality.getMunicipalityName());
                continue;
            }

            float avgIrsa = (float) history.stream()
                    .mapToDouble(i -> i.getIrsaValue())
                    .average()
                    .orElse(0.5);

            for (int d = 1; d <= daysAhead; d++) {
                LocalDate targetDate = today.plusDays(d);

                Irsa forecast = new Irsa();
                forecast.setMunicipality(municipality);
                forecast.setIrsaValue(avgIrsa);
                forecast.setRiskLevel(calculateLevel(avgIrsa));
                forecast.setIsForecast(true);
                forecast.setForecastDate(targetDate.atStartOfDay(cdmx).toInstant());
                irsaRepository.persist(forecast);
                generated++;
            }
        }
        LOG.infof("[FORECAST] Generated %d forecasts for the next %d days", generated, daysAhead);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────────

    private String calculateLevel(float value) {
        if (value <= 0.25f) return "LOW";
        if (value <= 0.50f) return "MODERATE";
        if (value <= 0.75f) return "HIGH";
        return "CRITICAL";
    }

    private double calculateAirScore(List<Long> stationIds, Instant from, Instant to) {
        if (stationIds.isEmpty()) {
            LOG.warn("[IRSA] Air: no stations, using neutral 50.0");
            return 50.0;
        }

        List<NO2>    no2List  = no2Repository.findByStationsAndDateRange(stationIds, from, to);
        List<O3>     o3List   = o3Repository.findByStationsAndDateRange(stationIds, from, to);
        List<PM25> pm25List = pm25Repository.findByStationsAndDateRange(stationIds, from, to);

        if (no2List.isEmpty() && o3List.isEmpty() && pm25List.isEmpty()) {
            LOG.warn("[IRSA] Air: no measurements in last 24h, using neutral 50.0");
            return 50.0;
        }

        List<Double> scores = new ArrayList<>();

        computeAverage(no2List.stream().map(NO2::getMetricValue).toList())
                .ifPresent(avg -> scores.add(Math.max(0, 100 - avg / 2.1)));
        computeAverage(o3List.stream().map(O3::getMetricValue).toList())
                .ifPresent(avg -> scores.add(Math.max(0, 100 - avg / 1.4)));
        computeAverage(pm25List.stream().map(PM25::getMetricValue).toList())
                .ifPresent(avg -> scores.add(Math.max(0, 100 - avg / 0.45)));

        return scores.isEmpty() ? 50.0 : scores.stream().mapToDouble(Double::doubleValue).average().orElse(50.0);
    }

    private double calculateClimateScore(List<Long> stationIds) {
        if (stationIds.isEmpty()) return 50.0;

        List<Temperature> temps = temperatureRepository.findLatestByStations(stationIds);
        if (temps.isEmpty()) {
            LOG.warn("[IRSA] Climate: no temperature data, using neutral 50.0");
            return 50.0;
        }

        java.util.OptionalDouble avgTemp = temps.stream()
                .mapToDouble(t -> parseDouble(t.getMetricValue()))
                .filter(v -> !Double.isNaN(v))
                .average();

        if (avgTemp.isEmpty()) return 50.0;

        // Optimal CDMX: 16-22°C, penalty 5 pts/°C from 19
        double score = 100 - Math.min(100, Math.abs(avgTemp.getAsDouble() - 19.0) * 5);
        return Math.max(0, score);
    }

    private double calculateSocioScore(Municipality municipality) {
        if (municipality.getSocialVulnerability() == null) {
            LOG.warnf("[IRSA] Socio: no social vulnerability for '%s', using neutral 50.0",
                    municipality.getMunicipalityName());
            return 50.0;
        }
        // CONEVAL index: [-2, 2] where lower = better → map to [100, 0]
        float v = municipality.getSocialVulnerability();
        double score = 100.0 - ((v + 2.0) / 4.0) * 100.0;
        return Math.max(0, Math.min(100, score));
    }

    private java.util.Optional<Double> computeAverage(List<String> values) {
        java.util.OptionalDouble avg = values.stream()
                .mapToDouble(this::parseDouble)
                .filter(v -> !Double.isNaN(v))
                .average();
        return avg.isPresent() ? java.util.Optional.of(avg.getAsDouble()) : java.util.Optional.empty();
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) return Double.NaN;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return Double.NaN; }
    }
}
