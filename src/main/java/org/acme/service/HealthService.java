package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.domain.entity.Asthma;
import org.acme.domain.entity.Copd;
import org.acme.domain.entity.Municipality;
import org.acme.domain.entity.Pneumonia;
import org.acme.domain.entity.Smoking;
import org.acme.dto.response.HealthSummaryResponse;
import org.acme.exception.AppException;
import org.acme.repository.AsthmaRepository;
import org.acme.repository.CopdRepository;
import org.acme.repository.MunicipalityRepository;
import org.acme.repository.PneumoniaRepository;
import org.acme.repository.SmokingRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class HealthService {

    private static final Logger LOG = Logger.getLogger(HealthService.class);

    // INEGI municipality code → municipality id in DB
    private static final Map<Integer, Long> INEGI_TO_MUNICIPALITY = Map.ofEntries(
            Map.entry(2,  2L),
            Map.entry(3,  4L),
            Map.entry(4,  5L),
            Map.entry(5,  7L),
            Map.entry(6,  8L),
            Map.entry(7,  9L),
            Map.entry(8,  10L),
            Map.entry(9,  12L),
            Map.entry(10, 1L),
            Map.entry(11, 13L),
            Map.entry(12, 14L),
            Map.entry(13, 16L),
            Map.entry(14, 3L),
            Map.entry(15, 6L),
            Map.entry(16, 11L),
            Map.entry(17, 15L)
    );

    @ConfigProperty(name = "health.csv.basepath", defaultValue = "docs/output_by_year")
    String csvBasePath;

    @Inject AsthmaRepository asthmaRepository;
    @Inject CopdRepository copdRepository;
    @Inject PneumoniaRepository pneumoniaRepository;
    @Inject SmokingRepository smokingRepository;
    @Inject MunicipalityRepository municipalityRepository;

    @Transactional
    public int importFromCsv(int year) {
        LOG.infof("[HEALTH] Starting import for year %d from %s", year, csvBasePath);

        Map<Long, Municipality> municipalities = new HashMap<>();
        municipalityRepository.listAll().forEach(m -> municipalities.put(m.getId(), m));

        int count = 0;
        count += importHealthRecords(year, "PNEUMONIA", municipalities, "PNEUMONIA");
        count += importHealthRecords(year, "COPD",      municipalities, "COPD");
        count += importHealthRecords(year, "ASTHMA",    municipalities, "ASTHMA");
        count += importHealthRecords(year, "SMOKING",   municipalities, "SMOKING");

        LOG.infof("[HEALTH] Import complete: %d records for year %d", count, year);
        return count;
    }

    private int importHealthRecords(int year, String folder, Map<Long, Municipality> municipalities, String type) {
        Path file = Paths.get(csvBasePath, folder, year + ".csv");
        if (!Files.exists(file)) {
            LOG.warnf("[HEALTH] File not found: %s", file);
            return 0;
        }

        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                if (cols.length < 6) continue;
                if (!"1".equals(cols[5].trim())) continue; // only positive cases

                Integer inegiCode = parseInteger(cols[1]);
                if (inegiCode == null) continue;

                Long municipalityId = INEGI_TO_MUNICIPALITY.get(inegiCode);
                if (municipalityId == null) continue;

                Municipality municipality = municipalities.get(municipalityId);
                if (municipality == null) continue;

                Integer age = parseInteger(cols[4]);
                Instant registeredAt = parseDate(cols[2]);

                switch (type) {
                    case "PNEUMONIA" -> {
                        Pneumonia p = new Pneumonia();
                        p.setMunicipality(municipality);
                        p.setAge(age != null ? age : 0);
                        p.setRegisteredAt(registeredAt);
                        pneumoniaRepository.persist(p);
                    }
                    case "COPD" -> {
                        Copd c = new Copd();
                        c.setMunicipality(municipality);
                        c.setAge(age != null ? age : 0);
                        c.setRegisteredAt(registeredAt);
                        copdRepository.persist(c);
                    }
                    case "ASTHMA" -> {
                        Asthma a = new Asthma();
                        a.setMunicipality(municipality);
                        a.setAge(age != null ? age : 0);
                        a.setRegisteredAt(registeredAt);
                        asthmaRepository.persist(a);
                    }
                    case "SMOKING" -> {
                        Smoking s = new Smoking();
                        s.setMunicipality(municipality);
                        s.setAge(age != null ? age : 0);
                        s.setRegisteredAt(registeredAt);
                        smokingRepository.persist(s);
                    }
                }
                count++;
            }
        } catch (IOException e) {
            LOG.errorf("[HEALTH] Error reading %s: %s", file, e.getMessage());
        }
        return count;
    }

    public double calculateHealthScore(Long municipalityId) {
        Instant since = Instant.now().minus(365, ChronoUnit.DAYS);
        long asthma   = asthmaRepository.countByMunicipalityAndSince(municipalityId, since);
        long copd     = copdRepository.countByMunicipalityAndSince(municipalityId, since);
        long pneumonia = pneumoniaRepository.countByMunicipalityAndSince(municipalityId, since);
        long smoking  = smokingRepository.countByMunicipalityAndSince(municipalityId, since);

        long total = asthma + copd + pneumonia + smoking;
        if (total == 0) {
            LOG.warnf("[IRSA] Health: no data for municipality %d, using neutral 50.0", municipalityId);
            return 50.0;
        }

        // More cases = lower health score; 500+ cases/year → 0 score
        double score = Math.max(0, 100.0 - (total / 5.0));
        LOG.infof("[IRSA] Health: municipality=%d | asthma=%d | copd=%d | pneumonia=%d | smoking=%d | score=%.2f",
                municipalityId, asthma, copd, pneumonia, smoking, score);
        return Math.min(100, score);
    }

    public HealthSummaryResponse getSummaryByMunicipality(Long municipalityId) {
        Municipality municipality = municipalityRepository.findByIdOptional(municipalityId)
                .orElseThrow(() -> AppException.notFound("Municipality not found"));

        long asthma   = asthmaRepository.countByMunicipalityAndSince(municipalityId, Instant.EPOCH);
        long copd     = copdRepository.countByMunicipalityAndSince(municipalityId, Instant.EPOCH);
        long pneumonia = pneumoniaRepository.countByMunicipalityAndSince(municipalityId, Instant.EPOCH);
        long smoking  = smokingRepository.countByMunicipalityAndSince(municipalityId, Instant.EPOCH);

        return new HealthSummaryResponse(
                municipalityId,
                municipality.getMunicipalityName(),
                asthma, copd, pneumonia, smoking,
                asthma + copd + pneumonia + smoking
        );
    }

    public List<HealthSummaryResponse> getAllSummaries() {
        return municipalityRepository.listAll().stream()
                .map(m -> getSummaryByMunicipality(m.getId()))
                .toList();
    }

    private Integer parseInteger(String value) {
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private Instant parseDate(String dateStr) {
        try {
            String clean = dateStr.trim();
            if (clean.length() < 10) return Instant.now();
            return java.time.LocalDate.parse(clean.substring(0, 10))
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant();
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
