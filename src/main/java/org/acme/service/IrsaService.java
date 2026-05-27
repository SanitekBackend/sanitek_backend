package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.NO2;
import org.acme.domain.entity.O3;
import org.acme.domain.entity.PM25;
import org.acme.domain.entity.Radiation;
import org.acme.domain.entity.Temperature;
import org.acme.domain.irsa.IrsaEngine;
import org.acme.domain.irsa.IrsaResult;
import org.acme.dto.response.IrsaDiagnosticResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaTrendResponse;
import org.acme.dto.response.TrendPoint;
import org.acme.exception.AppException;
import org.acme.mapper.IrsaMapper;
import org.acme.repository.AsthmaRepository;
import org.acme.repository.CopdRepository;
import org.acme.repository.IrsaRepository;
import org.acme.repository.MunicipalityRepository;
import org.acme.repository.NO2Repository;
import org.acme.repository.O3Repository;
import org.acme.repository.PM25Repository;
import org.acme.repository.PneumoniaRepository;
import org.acme.repository.RadiationRepository;
import org.acme.repository.SmokingRepository;
import org.acme.repository.StationRepository;
import org.acme.repository.TemperatureRepository;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class IrsaService {

    private static final Logger LOG = Logger.getLogger(IrsaService.class);

    @Inject IrsaRepository         irsaRepository;
    @Inject MunicipalityRepository municipalityRepository;
    @Inject StationRepository      stationRepository;
    @Inject NO2Repository          no2Repository;
    @Inject O3Repository           o3Repository;
    @Inject PM25Repository         pm25Repository;
    @Inject RadiationRepository    radiationRepository;
    @Inject TemperatureRepository  temperatureRepository;
    @Inject CopdRepository         copdRepository;
    @Inject AsthmaRepository       asthmaRepository;
    @Inject PneumoniaRepository    pneumoniaRepository;
    @Inject SmokingRepository      smokingRepository;
    @Inject IrsaMapper             irsaMapper;

    private final IrsaEngine engine = new IrsaEngine();

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

        List<Long> stationIds = stationRepository.findIdsByMunicipality(municipalityId);
        CalculationWindow window = resolveLatestMeasurementWindow(stationIds);

        double avgNo2  = averagePollutant(
                no2Repository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(NO2::getMetricValue).toList());

        double avgO3   = averagePollutant(
                o3Repository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(O3::getMetricValue).toList());

        double avgPm25 = averagePollutant(
                pm25Repository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(PM25::getMetricValue).toList());

        double avgUv   = averagePollutant(
                radiationRepository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(Radiation::getMetricValue).toList());

        double avgTmp  = averagePollutant(
                temperatureRepository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(Temperature::getMetricValue).toList());

        long copdCount      = copdRepository.countByMunicipality(municipalityId);
        long asthmaCount    = asthmaRepository.countTotalByMunicipality(municipalityId);
        long pneumoniaCount = pneumoniaRepository.countTotalByMunicipality(municipalityId);
        long smokingCount   = smokingRepository.countTotalByMunicipality(municipalityId);

        IrsaResult result = engine.calculate(
                avgNo2, avgO3, avgPm25, avgUv, avgTmp,
                copdCount, asthmaCount, pneumoniaCount, smokingCount);

        LOG.infof("[IRSA] Municipio=%s | Window=%s to %s | NO2=%.4f O3=%.4f PM25=%.4f UV=%.4f TMP=%.4f | C=%.4f | FV=%.4f | IRSA=%.2f | Nivel=%s",
                municipality.getMunicipalityName(),
                window.from(), window.to(),
                result.normNo2(), result.normO3(), result.normPm25(), result.normUv(), result.normTmp(),
                result.pollutantScore(), result.vulnerabilityFactor(),
                result.irsaScore(), result.riskLevel());

        Irsa irsa = buildIrsa(municipality, result);
        irsaRepository.persist(irsa);

        return irsaMapper.toResponse(irsa);
    }

    public IrsaDiagnosticResponse getDiagnostic(Long municipalityId) {
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        List<Long> stationIds = stationRepository.findIdsByMunicipality(municipalityId);
        CalculationWindow window = resolveLatestMeasurementWindow(stationIds);

        List<NO2>         no2List  = no2Repository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<O3>          o3List   = o3Repository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<PM25>        pm25List = pm25Repository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<Radiation>   uvList   = radiationRepository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<Temperature> tmpList  = temperatureRepository.findByStationsAndDateRange(stationIds, window.from(), window.to());

        double avgNo2  = averagePollutant(no2List.stream().map(NO2::getMetricValue).toList());
        double avgO3   = averagePollutant(o3List.stream().map(O3::getMetricValue).toList());
        double avgPm25 = averagePollutant(pm25List.stream().map(PM25::getMetricValue).toList());
        double avgUv   = averagePollutant(uvList.stream().map(Radiation::getMetricValue).toList());
        double avgTmp  = averagePollutant(tmpList.stream().map(Temperature::getMetricValue).toList());

        long copdCount      = copdRepository.countByMunicipality(municipalityId);
        long asthmaCount    = asthmaRepository.countTotalByMunicipality(municipalityId);
        long pneumoniaCount = pneumoniaRepository.countTotalByMunicipality(municipalityId);
        long smokingCount   = smokingRepository.countTotalByMunicipality(municipalityId);

        IrsaResult r = engine.calculate(
                avgNo2, avgO3, avgPm25, avgUv, avgTmp,
                copdCount, asthmaCount, pneumoniaCount, smokingCount);

        return new IrsaDiagnosticResponse(
                municipalityId,
                municipality.getMunicipalityName(),
                r.normNo2(),
                r.normO3(),
                r.normPm25(),
                r.normUv(),
                r.normTmp(),
                r.pollutantScore(),
                r.prevCopd(),
                r.prevAsthma(),
                r.prevPneumonia(),
                r.prevSmoking(),
                r.vulnerabilityFactor(),
                r.irsaScore(),
                r.riskLevel(),
                no2List.size(),
                o3List.size(),
                pm25List.size(),
                uvList.size(),
                tmpList.size(),
                copdCount,
                asthmaCount,
                pneumoniaCount,
                smokingCount
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
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

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

        List<TrendPoint> points = grouped.entrySet().stream()
                .map(e -> {
                    List<Double> values = e.getValue().stream()
                            .filter(i -> i.getIrsaValue() != null)
                            .map(i -> (double) i.getIrsaValue())
                            .toList();
                    double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                    double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);

                    String key = e.getKey();
                    String label;
                    if (monthly) {
                        int yr = Integer.parseInt(key.substring(0, 4));
                        int mo = Integer.parseInt(key.substring(5, 7));
                        label  = months[mo - 1] + " " + yr;
                    } else {
                        label = "W" + key.substring(6) + " (" + key.substring(0, 4) + ")";
                    }

                    return new TrendPoint(label, avg, min, max, IrsaResult.categorize(avg), values.size());
                })
                .toList();

        double variation = 0.0;
        String trend     = "STABLE";
        if (points.size() >= 2) {
            variation = points.getLast().avgIrsa() - points.getFirst().avgIrsa();
            if      (variation >  5.0) trend = "WORSENING";
            else if (variation < -5.0) trend = "IMPROVING";
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
        ZoneId    cdmx   = ZoneId.of("America/Mexico_City");
        LocalDate today  = LocalDate.now(cdmx);
        Instant   since7 = today.minusDays(7).atStartOfDay(cdmx).toInstant();
        Instant   now    = Instant.now();

        Instant rangeStart = today.plusDays(1).atStartOfDay(cdmx).toInstant();
        Instant rangeEnd   = today.plusDays(daysAhead + 1).atStartOfDay(cdmx).toInstant();
        long deleted = irsaRepository.deleteForecasts(rangeStart, rangeEnd);
        LOG.infof("[FORECAST] Deleted %d existing forecasts", deleted);

        List<Municipality> municipalities = municipalityRepository.listAll();
        int generated = 0;

        for (Municipality municipality : municipalities) {
            List<Irsa> history = irsaRepository.findHistoricalNoForecast(
                    municipality.getId(), since7, now);

            if (history.isEmpty()) {
                LOG.warnf("[FORECAST] No history for municipality '%s', skipping",
                        municipality.getMunicipalityName());
                continue;
            }

            float avgIrsa = (float) history.stream()
                    .filter(i -> i.getIrsaValue() != null)
                    .mapToDouble(i -> i.getIrsaValue())
                    .average()
                    .orElse(50.0);

            for (int d = 1; d <= daysAhead; d++) {
                LocalDate targetDate = today.plusDays(d);

                Irsa forecast = new Irsa();
                forecast.setMunicipality(municipality);
                forecast.setIrsaValue(avgIrsa);
                forecast.setRiskLevel(IrsaResult.categorize(avgIrsa));
                forecast.setIsForecast(true);
                forecast.setForecastDate(targetDate.atStartOfDay(cdmx).toInstant());
                irsaRepository.persist(forecast);
                generated++;
            }
        }
        LOG.infof("[FORECAST] Generated %d forecasts for the next %d days", generated, daysAhead);
    }

    private double averagePollutant(List<String> values) {
        if (values == null || values.isEmpty()) return 0.0;
        OptionalDouble avg = values.stream()
                .mapToDouble(this::parseDouble)
                .filter(v -> !Double.isNaN(v) && v >= 0.0)
                .average();
        return avg.isPresent() ? avg.getAsDouble() : 0.0;
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) return Double.NaN;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private Irsa buildIrsa(Municipality municipality, IrsaResult r) {
        Irsa irsa = new Irsa();
        irsa.setMunicipality(municipality);
        irsa.setIrsaValue((float) r.irsaScore());
        irsa.setRiskLevel(r.riskLevel());
        irsa.setIsForecast(false);
        irsa.setNormNo2(r.normNo2());
        irsa.setNormO3(r.normO3());
        irsa.setNormPm25(r.normPm25());
        irsa.setNormUv(r.normUv());
        irsa.setNormTmp(r.normTmp());
        irsa.setPollutantScore(r.pollutantScore());
        irsa.setPrevCopd(r.prevCopd());
        irsa.setPrevAsthma(r.prevAsthma());
        irsa.setPrevPneumonia(r.prevPneumonia());
        irsa.setPrevSmoking(r.prevSmoking());
        irsa.setVulnerabilityFactor(r.vulnerabilityFactor());
        return irsa;
    }

    private CalculationWindow resolveLatestMeasurementWindow(List<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            throw AppException.notFound("No stations found for this municipality");
        }

        Instant latest = List.of(
                        no2Repository.findLatestRegisteredAtByStations(stationIds),
                        o3Repository.findLatestRegisteredAtByStations(stationIds),
                        pm25Repository.findLatestRegisteredAtByStations(stationIds),
                        radiationRepository.findLatestRegisteredAtByStations(stationIds),
                        temperatureRepository.findLatestRegisteredAtByStations(stationIds)
                ).stream()
                .flatMap(Optional::stream)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> AppException.notFound("No measurement data found for this municipality"));

        return new CalculationWindow(latest.minus(24, ChronoUnit.HOURS), latest);
    }

    private record CalculationWindow(Instant from, Instant to) {}
}
