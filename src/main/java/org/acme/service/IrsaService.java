package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.narayana.jta.QuarkusTransaction;
import org.acme.domain.entity.Irsa;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.NO2;
import org.acme.domain.entity.O3;
import org.acme.domain.entity.PM25;
import org.acme.domain.entity.Radiation;
import org.acme.domain.entity.Temperature;
import org.acme.domain.irsa.IrsaEngine;
import org.acme.domain.irsa.IrsaResult;
import org.acme.dto.response.IrsaBackfillResponse;
import org.acme.dto.response.IrsaDiagnosticResponse;
import org.acme.dto.response.IrsaResponse;
import org.acme.dto.response.IrsaTimelineMunicipalityPoint;
import org.acme.dto.response.IrsaTimelineSnapshotResponse;
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
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

    public List<Long> listAllMunicipalityIds() {
        return municipalityRepository.listAll().stream()
                .map(Municipality::getId)
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

        double avgNo2  = averagePollutantOrLastValid(
                no2Repository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(NO2::getMetricValue).toList(),
                () -> no2Repository.findRecentValuesByStations(stationIds, 100),
                () -> no2Repository.findCityWideRecentValues(500),
                "NO2");

        double avgO3   = averagePollutantOrLastValid(
                o3Repository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(O3::getMetricValue).toList(),
                () -> o3Repository.findRecentValuesByStations(stationIds, 100),
                () -> o3Repository.findCityWideRecentValues(500),
                "O3");

        double avgPm25 = averagePollutantOrLastValid(
                pm25Repository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(PM25::getMetricValue).toList(),
                () -> pm25Repository.findRecentValuesByStations(stationIds, 100),
                () -> pm25Repository.findCityWideRecentValues(500),
                "PM25");

        double avgUv   = averagePollutantOrLastValid(
                radiationRepository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(Radiation::getMetricValue).toList(),
                () -> radiationRepository.findRecentValuesByStations(stationIds, 100),
                () -> radiationRepository.findCityWideRecentValues(500),
                "UV");

        double avgTmp  = averagePollutantOrLastValid(
                temperatureRepository.findByStationsAndDateRange(stationIds, window.from(), window.to())
                        .stream().map(Temperature::getMetricValue).toList(),
                () -> temperatureRepository.findRecentValuesByStations(stationIds, 100),
                () -> temperatureRepository.findCityWideRecentValues(500),
                "TMP");

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
        irsa.setPeriodStart(window.from());
        irsa.setPeriodEnd(window.to());
        irsaRepository.persist(irsa);

        return irsaMapper.toResponse(irsa);
    }

    public IrsaDiagnosticResponse getDiagnostic(Long municipalityId) {
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        LOG.infof("[DIAGNOSTIC] Calculando para municipalityId=%d (%s)",
                municipalityId, municipality.getMunicipalityName());

        List<Long> stationIds = stationRepository.findIdsByMunicipality(municipalityId);
        CalculationWindow window = resolveLatestMeasurementWindow(stationIds);

        List<NO2>         no2List  = no2Repository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<O3>          o3List   = o3Repository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<PM25>        pm25List = pm25Repository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<Radiation>   uvList   = radiationRepository.findByStationsAndDateRange(stationIds, window.from(), window.to());
        List<Temperature> tmpList  = temperatureRepository.findByStationsAndDateRange(stationIds, window.from(), window.to());

        double avgNo2  = averagePollutantOrLastValid(
                no2List.stream().map(NO2::getMetricValue).toList(),
                () -> no2Repository.findRecentValuesByStations(stationIds, 100),
                () -> no2Repository.findCityWideRecentValues(500),
                "NO2");

        double avgO3   = averagePollutantOrLastValid(
                o3List.stream().map(O3::getMetricValue).toList(),
                () -> o3Repository.findRecentValuesByStations(stationIds, 100),
                () -> o3Repository.findCityWideRecentValues(500),
                "O3");

        double avgPm25 = averagePollutantOrLastValid(
                pm25List.stream().map(PM25::getMetricValue).toList(),
                () -> pm25Repository.findRecentValuesByStations(stationIds, 100),
                () -> pm25Repository.findCityWideRecentValues(500),
                "PM25");

        double avgUv   = averagePollutantOrLastValid(
                uvList.stream().map(Radiation::getMetricValue).toList(),
                () -> radiationRepository.findRecentValuesByStations(stationIds, 100),
                () -> radiationRepository.findCityWideRecentValues(500),
                "UV");

        double avgTmp  = averagePollutantOrLastValid(
                tmpList.stream().map(Temperature::getMetricValue).toList(),
                () -> temperatureRepository.findRecentValuesByStations(stationIds, 100),
                () -> temperatureRepository.findCityWideRecentValues(500),
                "TMP");

        long copdCount      = copdRepository.countByMunicipality(municipalityId);
        long asthmaCount    = asthmaRepository.countTotalByMunicipality(municipalityId);
        long pneumoniaCount = pneumoniaRepository.countTotalByMunicipality(municipalityId);
        long smokingCount   = smokingRepository.countTotalByMunicipality(municipalityId);

        IrsaResult r = engine.calculate(
                avgNo2, avgO3, avgPm25, avgUv, avgTmp,
                copdCount, asthmaCount, pneumoniaCount, smokingCount);

        LOG.infof("[DIAGNOSTIC] Municipio=%s | Window=%s to %s | NO2=%.4f O3=%.4f PM25=%.4f UV=%.4f TMP=%.4f | Score=%.4f | FV=%.4f | IRSA=%.2f | Nivel=%s",
                municipality.getMunicipalityName(),
                window.from(), window.to(),
                r.normNo2(), r.normO3(), r.normPm25(), r.normUv(), r.normTmp(),
                r.pollutantScore(), r.vulnerabilityFactor(),
                r.irsaScore(), r.riskLevel());

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

    public IrsaTimelineSnapshotResponse getTimelineSnapshot(int offsetDays) {
        validateDaysBack(offsetDays);
        Map<TimelineCityCacheKey, Double> cityAverageCache = new HashMap<>();

        List<IrsaTimelineMunicipalityPoint> municipalities = municipalityRepository.listAll().stream()
                .sorted(Comparator.comparing(Municipality::getId))
                .map(municipality -> buildTimelineSnapshotPoint(municipality, offsetDays, cityAverageCache))
                .toList();

        return new IrsaTimelineSnapshotResponse(offsetDays, timelineLabel(offsetDays), municipalities);
    }

    private IrsaTimelineMunicipalityPoint buildTimelineSnapshotPoint(Municipality municipality,
                                                                     int offsetDays,
                                                                     Map<TimelineCityCacheKey, Double> cityAverageCache) {
        try {
            Long municipalityId = municipality.getId();
            List<Long> stationIds = stationRepository.findIdsByMunicipality(municipalityId);
            Instant latestMeasurementAt = resolveLatestMeasurementWindow(stationIds).to();
            Instant windowTo = latestMeasurementAt.minus(offsetDays, ChronoUnit.DAYS);
            Instant windowFrom = windowTo.minus(24, ChronoUnit.HOURS);

            long copdCount      = copdRepository.countByMunicipality(municipalityId);
            long asthmaCount    = asthmaRepository.countTotalByMunicipality(municipalityId);
            long pneumoniaCount = pneumoniaRepository.countTotalByMunicipality(municipalityId);
            long smokingCount   = smokingRepository.countTotalByMunicipality(municipalityId);

            double avgNo2  = averagePollutantOrLastValidBefore(
                    no2Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                            .stream().map(NO2::getMetricValue).toList(),
                    () -> no2Repository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                    () -> cachedCityAverage(cityAverageCache, "NO2", windowTo,
                            () -> no2Repository.findCityWideRecentValuesBefore(windowTo, 500)),
                    "NO2",
                    windowTo);

            double avgO3   = averagePollutantOrLastValidBefore(
                    o3Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                            .stream().map(O3::getMetricValue).toList(),
                    () -> o3Repository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                    () -> cachedCityAverage(cityAverageCache, "O3", windowTo,
                            () -> o3Repository.findCityWideRecentValuesBefore(windowTo, 500)),
                    "O3",
                    windowTo);

            double avgPm25 = averagePollutantOrLastValidBefore(
                    pm25Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                            .stream().map(PM25::getMetricValue).toList(),
                    () -> pm25Repository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                    () -> cachedCityAverage(cityAverageCache, "PM25", windowTo,
                            () -> pm25Repository.findCityWideRecentValuesBefore(windowTo, 500)),
                    "PM25",
                    windowTo);

            double avgUv   = averagePollutantOrLastValidBefore(
                    radiationRepository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                            .stream().map(Radiation::getMetricValue).toList(),
                    () -> radiationRepository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                    () -> cachedCityAverage(cityAverageCache, "UV", windowTo,
                            () -> radiationRepository.findCityWideRecentValuesBefore(windowTo, 500)),
                    "UV",
                    windowTo);

            double avgTmp  = averagePollutantOrLastValidBefore(
                    temperatureRepository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                            .stream().map(Temperature::getMetricValue).toList(),
                    () -> temperatureRepository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                    () -> cachedCityAverage(cityAverageCache, "TMP", windowTo,
                            () -> temperatureRepository.findCityWideRecentValuesBefore(windowTo, 500)),
                    "TMP",
                    windowTo);

            IrsaResult r = engine.calculate(
                    avgNo2, avgO3, avgPm25, avgUv, avgTmp,
                    copdCount, asthmaCount, pneumoniaCount, smokingCount);

            return new IrsaTimelineMunicipalityPoint(
                    municipalityId,
                    municipality.getMunicipalityName(),
                    offsetDays,
                    timelineLabel(offsetDays),
                    latestMeasurementAt,
                    windowFrom,
                    windowTo,
                    r.irsaScore(),
                    r.riskLevel(),
                    r.pollutantScore(),
                    r.vulnerabilityFactor(),
                    "OK",
                    null
            );
        } catch (Exception e) {
            LOG.warnf("[TIMELINE] municipalityId=%s offsetDays=%d skipped: %s",
                    municipality.getId(), offsetDays, e.getMessage());
            return new IrsaTimelineMunicipalityPoint(
                    municipality.getId(),
                    municipality.getMunicipalityName(),
                    offsetDays,
                    timelineLabel(offsetDays),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "ERROR",
                    e.getMessage()
            );
        }
    }

    private void validateDaysBack(int daysBack) {
        if (daysBack < 0 || daysBack > 30) {
            throw AppException.badRequest("daysBack must be between 0 and 30");
        }
    }

    public IrsaBackfillResponse backfillMonthly(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw AppException.badRequest("from and to are required in YYYY-MM-DD format");
        }
        if (!fromDate.isBefore(toDate)) {
            throw AppException.badRequest("from must be before to");
        }

        ZoneId cdmx = ZoneId.of("America/Mexico_City");
        YearMonth fromMonth = YearMonth.from(fromDate);
        YearMonth toMonthExclusive = YearMonth.from(toDate);
        if (toDate.getDayOfMonth() > 1) {
            toMonthExclusive = toMonthExclusive.plusMonths(1);
        }

        Instant rangeStart = fromMonth.atDay(1).atStartOfDay(cdmx).toInstant();
        Instant rangeEnd = toMonthExclusive.atDay(1).atStartOfDay(cdmx).toInstant();

        List<Municipality> municipalities = municipalityRepository.listAll().stream()
                .sorted(Comparator.comparing(Municipality::getId))
                .toList();
        List<Long> cityStationIds = stationRepository.listAll().stream()
                .map(station -> station.getId())
                .toList();
        Map<TimelineCityCacheKey, Double> cityAverageCache = new HashMap<>();
        List<String> errors = new ArrayList<>();

        int months = 0;
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;

        LOG.infof("[IRSA-BACKFILL] Monthly backfill starting from=%s to=%s months=%d municipalities=%d",
                rangeStart, rangeEnd, java.time.temporal.ChronoUnit.MONTHS.between(fromMonth, toMonthExclusive),
                municipalities.size());

        for (YearMonth month = fromMonth; month.isBefore(toMonthExclusive); month = month.plusMonths(1)) {
            months++;
            Instant periodStart = month.atDay(1).atStartOfDay(cdmx).toInstant();
            Instant periodEnd = month.plusMonths(1).atDay(1).atStartOfDay(cdmx).toInstant();

            LOG.infof("[IRSA-BACKFILL] Calculating month=%s periodStart=%s periodEnd=%s",
                    month, periodStart, periodEnd);

            for (Municipality municipality : municipalities) {
                try {
                    List<Long> stationIds = stationRepository.findIdsByMunicipality(municipality.getId());
                    if (stationIds == null || stationIds.isEmpty()) {
                        throw AppException.notFound("No stations found for this municipality");
                    }

                    Optional<IrsaResult> result = calculateMonthlyResultForWindow(
                            municipality.getId(),
                            stationIds,
                            cityStationIds,
                            periodStart,
                            periodEnd,
                            cityAverageCache
                    );
                    if (result.isEmpty()) {
                        if (deleteMonthlyIrsa(municipality.getId(), periodStart, periodEnd)) {
                            LOG.infof("[IRSA-BACKFILL] Deleted stale no-data record month=%s municipality=%s",
                                    month, municipality.getMunicipalityName());
                        }
                        skipped++;
                        continue;
                    }

                    boolean createdRecord = upsertMonthlyIrsa(
                            municipality.getId(),
                            periodStart,
                            periodEnd,
                            result.get()
                    );
                    if (createdRecord) {
                        created++;
                    } else {
                        updated++;
                    }
                } catch (Exception e) {
                    failed++;
                    String error = "%s %s: %s".formatted(
                            month,
                            municipality.getMunicipalityName(),
                            e.getMessage()
                    );
                    errors.add(error);
                    LOG.warnf("[IRSA-BACKFILL] %s", error);
                }
            }
        }

        LOG.infof("[IRSA-BACKFILL] Monthly backfill done from=%s to=%s months=%d municipalities=%d created=%d updated=%d skipped=%d failed=%d",
                rangeStart, rangeEnd, months, municipalities.size(), created, updated, skipped, failed);

        return new IrsaBackfillResponse(
                rangeStart,
                rangeEnd,
                months,
                municipalities.size(),
                created,
                updated,
                skipped,
                failed,
                errors
        );
    }

    private boolean upsertMonthlyIrsa(Long municipalityId,
                                      Instant periodStart,
                                      Instant periodEnd,
                                      IrsaResult result) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Irsa irsa = irsaRepository
                    .findByMunicipalityAndPeriod(municipalityId, periodStart, periodEnd)
                    .orElse(null);

            if (irsa == null) {
                Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                        .orElseThrow(() -> AppException.notFound("Municipality not found"));
                irsa = buildIrsa(municipality, result);
                irsa.setPeriodStart(periodStart);
                irsa.setPeriodEnd(periodEnd);
                irsaRepository.persist(irsa);
                return true;
            }

            applyIrsaResult(irsa, result);
            irsa.setPeriodStart(periodStart);
            irsa.setPeriodEnd(periodEnd);
            return false;
        });
    }

    private boolean deleteMonthlyIrsa(Long municipalityId, Instant periodStart, Instant periodEnd) {
        return QuarkusTransaction.requiringNew().call(() ->
                irsaRepository.delete("municipality.id = ?1 AND periodStart = ?2 AND periodEnd = ?3 AND isForecast = false",
                        municipalityId, periodStart, periodEnd) > 0
        );
    }

    public IrsaTrendResponse getTrend(Long municipalityId, String period, int count) {
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        boolean monthly = "MONTHLY".equalsIgnoreCase(period);
        if (monthly) {
            return getMonthlyTrend(municipality, count);
        }

        Instant to   = Instant.now();
        Instant from = to.minus(count * 7L, ChronoUnit.DAYS);

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

    private IrsaTrendResponse getMonthlyTrend(Municipality municipality, int count) {
        int safeCount = Math.max(count, 1);
        ZoneId cdmx = ZoneId.of("America/Mexico_City");
        YearMonth currentMonth = YearMonth.now(cdmx);
        YearMonth fromMonth = currentMonth.minusMonths(safeCount - 1L);
        Instant from = fromMonth.atDay(1).atStartOfDay(cdmx).toInstant();
        Instant to = currentMonth.plusMonths(1).atDay(1).atStartOfDay(cdmx).toInstant();

        List<Irsa> records = irsaRepository.findHistoricalByMunicipalityPeriod(municipality.getId(), from, to);
        if (records.isEmpty()) {
            return new IrsaTrendResponse(
                    municipality.getId(),
                    municipality.getMunicipalityName(),
                    "MONTHLY",
                    safeCount,
                    "NO_DATA",
                    0.0,
                    List.of()
            );
        }

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        Map<String, List<Irsa>> grouped = new TreeMap<>(records.stream()
                .filter(i -> i.getPeriodStart() != null)
                .collect(Collectors.groupingBy(i -> {
                    ZonedDateTime zdt = i.getPeriodStart().atZone(cdmx);
                    return String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
                })));

        List<TrendPoint> points = grouped.entrySet().stream()
                .map(e -> {
                    List<Double> values = e.getValue().stream()
                            .filter(i -> i.getIrsaValue() != null)
                            .map(i -> (double) i.getIrsaValue())
                            .toList();
                    double avg = round1(values.stream().mapToDouble(Double::doubleValue).average().orElse(0));
                    double min = round1(values.stream().mapToDouble(Double::doubleValue).min().orElse(0));
                    double max = round1(values.stream().mapToDouble(Double::doubleValue).max().orElse(0));

                    int year = Integer.parseInt(e.getKey().substring(0, 4));
                    int month = Integer.parseInt(e.getKey().substring(5, 7));
                    String label = months[month - 1] + " " + year;

                    return new TrendPoint(label, avg, min, max, IrsaResult.categorize(avg), values.size());
                })
                .toList();

        double variation = 0.0;
        String trend = "STABLE";
        if (points.size() >= 2) {
            variation = round1(points.getLast().avgIrsa() - points.getFirst().avgIrsa());
            if      (variation >  5.0) trend = "WORSENING";
            else if (variation < -5.0) trend = "IMPROVING";
        }

        return new IrsaTrendResponse(
                municipality.getId(),
                municipality.getMunicipalityName(),
                "MONTHLY",
                safeCount,
                trend,
                variation,
                points
        );
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
                .filter(v -> !Double.isNaN(v) && v > 0.0)   // excluye 0 además de NaN
                .average();
        return avg.isPresent() ? avg.getAsDouble() : 0.0;
    }

    private Optional<IrsaResult> calculateMonthlyResultForWindow(Long municipalityId,
                                                                 List<Long> stationIds,
                                                                 List<Long> cityStationIds,
                                                                 Instant windowFrom,
                                                                 Instant windowTo,
                                                                 Map<TimelineCityCacheKey, Double> cityAverageCache) {
        double avgNo2 = averageMonthlyPollutant(
                no2Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(NO2::getMetricValue).toList(),
                () -> cachedCityAverage(cityAverageCache, "NO2", windowTo,
                        () -> no2Repository.findByStationsAndDateRange(cityStationIds, windowFrom, windowTo)
                                .stream().map(NO2::getMetricValue).toList()),
                "NO2",
                windowFrom,
                windowTo
        );

        double avgO3 = averageMonthlyPollutant(
                o3Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(O3::getMetricValue).toList(),
                () -> cachedCityAverage(cityAverageCache, "O3", windowTo,
                        () -> o3Repository.findByStationsAndDateRange(cityStationIds, windowFrom, windowTo)
                                .stream().map(O3::getMetricValue).toList()),
                "O3",
                windowFrom,
                windowTo
        );

        double avgPm25 = averageMonthlyPollutant(
                pm25Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(PM25::getMetricValue).toList(),
                () -> cachedCityAverage(cityAverageCache, "PM25", windowTo,
                        () -> pm25Repository.findByStationsAndDateRange(cityStationIds, windowFrom, windowTo)
                                .stream().map(PM25::getMetricValue).toList()),
                "PM25",
                windowFrom,
                windowTo
        );

        double avgUv = averageMonthlyPollutant(
                radiationRepository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(Radiation::getMetricValue).toList(),
                () -> cachedCityAverage(cityAverageCache, "UV", windowTo,
                        () -> radiationRepository.findByStationsAndDateRange(cityStationIds, windowFrom, windowTo)
                                .stream().map(Radiation::getMetricValue).toList()),
                "UV",
                windowFrom,
                windowTo
        );

        double avgTmp = averageMonthlyPollutant(
                temperatureRepository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(Temperature::getMetricValue).toList(),
                () -> cachedCityAverage(cityAverageCache, "TMP", windowTo,
                        () -> temperatureRepository.findByStationsAndDateRange(cityStationIds, windowFrom, windowTo)
                                .stream().map(Temperature::getMetricValue).toList()),
                "TMP",
                windowFrom,
                windowTo
        );

        long copdCount      = copdRepository.countByMunicipality(municipalityId);
        long asthmaCount    = asthmaRepository.countTotalByMunicipality(municipalityId);
        long pneumoniaCount = pneumoniaRepository.countTotalByMunicipality(municipalityId);
        long smokingCount   = smokingRepository.countTotalByMunicipality(municipalityId);

        return Optional.of(engine.calculate(
                avgNo2, avgO3, avgPm25, avgUv, avgTmp,
                copdCount, asthmaCount, pneumoniaCount, smokingCount));
    }

    private double averageMonthlyPollutant(List<String> localValues,
                                           java.util.function.Supplier<Double> cityAverageFn,
                                           String pollutantName,
                                           Instant windowFrom,
                                           Instant windowTo) {
        double localAvg = averagePollutant(localValues);
        if (localAvg > 0.0) {
            return localAvg;
        }

        double cityAvg = cityAverageFn.get();
        if (cityAvg > 0.0) {
            LOG.debugf("[IRSA-BACKFILL] %s from=%s to=%s: no monthly local data -> monthly city average %.4f",
                    pollutantName, windowFrom, windowTo, cityAvg);
            return cityAvg;
        }

        double estimated = estimateMonthlyPollutant(pollutantName, windowFrom);
        LOG.debugf("[IRSA-BACKFILL] %s from=%s to=%s: no monthly local or city data -> estimated %.4f",
                pollutantName, windowFrom, windowTo, estimated);
        return estimated;
    }

    private double estimateMonthlyPollutant(String pollutantName, Instant windowFrom) {
        int month = windowFrom.atZone(ZoneId.of("America/Mexico_City")).getMonthValue();
        return switch (pollutantName) {
            case "NO2" -> 15.0 * seasonalFactor(month,
                    1.18, 1.12, 1.03, 0.98, 0.94, 0.90, 0.88, 0.90, 0.96, 1.05, 1.12, 1.20);
            case "O3" -> 58.0 * seasonalFactor(month,
                    0.78, 0.88, 1.08, 1.25, 1.30, 1.18, 1.05, 0.98, 0.94, 0.90, 0.84, 0.78);
            case "PM25" -> 9.5 * seasonalFactor(month,
                    1.28, 1.14, 1.05, 1.00, 1.18, 0.96, 0.88, 0.86, 0.90, 0.98, 1.08, 1.22);
            case "UV" -> 3.8 * seasonalFactor(month,
                    0.72, 0.88, 1.08, 1.24, 1.30, 1.20, 1.10, 1.02, 0.92, 0.82, 0.74, 0.68);
            case "TMP" -> 23.0 * seasonalFactor(month,
                    0.82, 0.90, 1.02, 1.14, 1.20, 1.12, 1.05, 1.02, 0.98, 0.92, 0.86, 0.80);
            default -> 1.0;
        };
    }

    private double seasonalFactor(int month, double... factors) {
        if (month < 1 || month > 12 || factors.length != 12) {
            return 1.0;
        }
        return factors[month - 1];
    }

    private IrsaResult calculateResultForWindow(Long municipalityId,
                                                List<Long> stationIds,
                                                Instant windowFrom,
                                                Instant windowTo,
                                                Map<TimelineCityCacheKey, Double> cityAverageCache) {
        double avgNo2  = averagePollutantOrLastValidBefore(
                no2Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(NO2::getMetricValue).toList(),
                () -> no2Repository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                () -> cachedCityAverage(cityAverageCache, "NO2", windowTo,
                        () -> no2Repository.findCityWideRecentValuesBefore(windowTo, 500)),
                "NO2",
                windowTo);

        double avgO3   = averagePollutantOrLastValidBefore(
                o3Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(O3::getMetricValue).toList(),
                () -> o3Repository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                () -> cachedCityAverage(cityAverageCache, "O3", windowTo,
                        () -> o3Repository.findCityWideRecentValuesBefore(windowTo, 500)),
                "O3",
                windowTo);

        double avgPm25 = averagePollutantOrLastValidBefore(
                pm25Repository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(PM25::getMetricValue).toList(),
                () -> pm25Repository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                () -> cachedCityAverage(cityAverageCache, "PM25", windowTo,
                        () -> pm25Repository.findCityWideRecentValuesBefore(windowTo, 500)),
                "PM25",
                windowTo);

        double avgUv   = averagePollutantOrLastValidBefore(
                radiationRepository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(Radiation::getMetricValue).toList(),
                () -> radiationRepository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                () -> cachedCityAverage(cityAverageCache, "UV", windowTo,
                        () -> radiationRepository.findCityWideRecentValuesBefore(windowTo, 500)),
                "UV",
                windowTo);

        double avgTmp  = averagePollutantOrLastValidBefore(
                temperatureRepository.findByStationsAndDateRange(stationIds, windowFrom, windowTo)
                        .stream().map(Temperature::getMetricValue).toList(),
                () -> temperatureRepository.findRecentValuesByStationsBefore(stationIds, windowTo, 100),
                () -> cachedCityAverage(cityAverageCache, "TMP", windowTo,
                        () -> temperatureRepository.findCityWideRecentValuesBefore(windowTo, 500)),
                "TMP",
                windowTo);

        long copdCount      = copdRepository.countByMunicipality(municipalityId);
        long asthmaCount    = asthmaRepository.countTotalByMunicipality(municipalityId);
        long pneumoniaCount = pneumoniaRepository.countTotalByMunicipality(municipalityId);
        long smokingCount   = smokingRepository.countTotalByMunicipality(municipalityId);

        return engine.calculate(
                avgNo2, avgO3, avgPm25, avgUv, avgTmp,
                copdCount, asthmaCount, pneumoniaCount, smokingCount);
    }

    /**
     * Cadena de fallback de 3 niveles para obtener un valor de contaminante:
     * <ol>
     *   <li>Promedio del window de 24 h de las estaciones de la alcaldia.</li>
     *   <li>Si todo es 0/"$": primer valor valido de los ultimos 100 registros historicos
     *       de esas mismas estaciones.</li>
     *   <li>Si sigue sin datos: promedio de los ultimos 500 registros de TODA la ciudad,
     *       reflejando condiciones actuales de CDMX.</li>
     * </ol>
     *
     * @param pollutantName  nombre del contaminante, solo para el log de diagnostico.
     * @param cityWideFn     supplier que retorna valores recientes de toda la ciudad.
     */
    private double averagePollutantOrLastValid(List<String> windowValues,
                                               java.util.function.Supplier<List<String>> recentValuesFn,
                                               java.util.function.Supplier<List<String>> cityWideFn,
                                               String pollutantName) {
        // Nivel 1 - window de la alcaldia
        double avg = averagePollutant(windowValues);
        if (avg > 0.0) return avg;

        // Nivel 2 - historial reciente de las estaciones de la alcaldia
        List<String> recent = recentValuesFn.get();
        OptionalDouble lastValid = recent.stream()
                .mapToDouble(this::parseDouble)
                .filter(v -> !Double.isNaN(v) && v > 0.0)
                .findFirst();

        if (lastValid.isPresent()) {
            LOG.debugf("[FALLBACK-LOCAL] %s: window=%d med. invalidas -> ultimo valor historico: %.4f",
                    pollutantName, windowValues.size(), lastValid.getAsDouble());
            return lastValid.getAsDouble();
        }

        // Nivel 3 - promedio ciudad (todas las alcaldias)
        List<String> cityValues = cityWideFn.get();
        double cityAvg = averagePollutant(cityValues);
        if (cityAvg > 0.0) {
            LOG.infof("[FALLBACK-CIUDAD] %s: alcaldia sin datos historicos -> promedio ciudad: %.4f",
                    pollutantName, cityAvg);
            return cityAvg;
        }

        LOG.warnf("[FALLBACK] %s: sin datos en window (%d), historial (%d) ni ciudad (%d) -> 0.0",
                pollutantName, windowValues.size(), recent.size(), cityValues.size());
        return 0.0;
    }

    private double averagePollutantOrLastValidBefore(List<String> windowValues,
                                                     java.util.function.Supplier<List<String>> recentValuesFn,
                                                     java.util.function.Supplier<Double> cityAverageFn,
                                                     String pollutantName,
                                                     Instant before) {
        double avg = averagePollutant(windowValues);
        if (avg > 0.0) return avg;

        List<String> recent = recentValuesFn.get();
        OptionalDouble lastValid = recent.stream()
                .mapToDouble(this::parseDouble)
                .filter(v -> !Double.isNaN(v) && v > 0.0)
                .findFirst();

        if (lastValid.isPresent()) {
            LOG.debugf("[FALLBACK-LOCAL-HIST] %s before=%s: window=%d med. invalidas -> ultimo valor historico: %.4f",
                    pollutantName, before, windowValues.size(), lastValid.getAsDouble());
            return lastValid.getAsDouble();
        }

        double cityAvg = cityAverageFn.get();
        if (cityAvg > 0.0) {
            LOG.debugf("[FALLBACK-CIUDAD-HIST] %s before=%s: alcaldia sin datos historicos -> promedio ciudad: %.4f",
                    pollutantName, before, cityAvg);
            return cityAvg;
        }

        LOG.debugf("[FALLBACK-HIST] %s before=%s: sin datos en window (%d), historial (%d) ni ciudad -> 0.0",
                pollutantName, before, windowValues.size(), recent.size());
        return 0.0;
    }

    private double cachedCityAverage(Map<TimelineCityCacheKey, Double> cityAverageCache,
                                     String pollutantName,
                                     Instant before,
                                     java.util.function.Supplier<List<String>> cityValuesFn) {
        return cityAverageCache.computeIfAbsent(
                new TimelineCityCacheKey(pollutantName, before),
                key -> averagePollutant(cityValuesFn.get())
        );
    }

    private record TimelineCityCacheKey(String pollutantName, Instant before) {}

    private String timelineLabel(int offsetDays) {
        if (offsetDays == 0) return "Ultimo";
        if (offsetDays == 1) return "Hace 1 dia";
        return "Hace " + offsetDays + " dias";
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
        applyIrsaResult(irsa, r);
        return irsa;
    }

    private void applyIrsaResult(Irsa irsa, IrsaResult r) {
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
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
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
