package org.acme.domain.irsa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("IrsaResult.categorize — clasificación del nivel de riesgo")
class IrsaResultTest {

    // ── Límite inferior: LOW ──────────────────────────────────────────────────

    @Test
    @DisplayName("score = 0.0 → LOW")
    void score0_LOW() {
        assertEquals("LOW", IrsaResult.categorize(0.0));
    }

    @Test
    @DisplayName("score = 40.0 (límite exacto) → LOW")
    void score35_LOW() {
        assertEquals("LOW", IrsaResult.categorize(35.0));
    }

    @Test
    @DisplayName("score = 20.0 (zona media LOW) → LOW")
    void score20_LOW() {
        assertEquals("LOW", IrsaResult.categorize(20.0));
    }

    // ── Zona MODERATE ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("score = 40.001 (justo sobre límite LOW) → MODERATE")
    void score35001_MODERATE() {
        assertEquals("MODERATE", IrsaResult.categorize(35.001));
    }

    @Test
    @DisplayName("score = 55.0 (zona media MODERATE) → MODERATE")
    void score37_MODERATE() {
        assertEquals("MODERATE", IrsaResult.categorize(37.0));
    }

    @Test
    @DisplayName("score = 70.0 (límite exacto MODERATE) → MODERATE")
    void score39_MODERATE() {
        assertEquals("MODERATE", IrsaResult.categorize(39.0));
    }

    // ── Zona HIGH ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("score = 70.001 (justo sobre límite MODERATE) → HIGH")
    void score39001_HIGH() {
        assertEquals("HIGH", IrsaResult.categorize(39.001));
    }

    @Test
    @DisplayName("score = 85.0 (zona alta HIGH) → HIGH")
    void score85_HIGH() {
        assertEquals("HIGH", IrsaResult.categorize(85.0));
    }

    @Test
    @DisplayName("score = 100.0 (máximo posible) → HIGH")
    void score100_HIGH() {
        assertEquals("HIGH", IrsaResult.categorize(100.0));
    }

    // ── Prueba parametrizada con todos los rangos ─────────────────────────────

    @ParameterizedTest(name = "score={0} → {1}")
    @CsvSource({
            "0.0,   LOW",
            "15.0,  LOW",
            "35.0,  LOW",
            "35.1,  MODERATE",
            "37.0,  MODERATE",
            "39.0,  MODERATE",
            "39.1,  HIGH",
            "90.0,  HIGH",
            "100.0, HIGH"
    })
    @DisplayName("tabla de equivalencias de niveles de riesgo")
    void tablaEquivalencias(double score, String expected) {
        assertEquals(expected, IrsaResult.categorize(score));
    }
}
