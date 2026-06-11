package org.acme.domain.irsa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IrsaEngine — cálculo del índice IRSA")
class IrsaEngineTest {

    private IrsaEngine engine;

    @BeforeEach
    void setUp() {
        engine = new IrsaEngine();
    }

    // ── Caso base ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("todos los contaminantes en cero → IRSA = 0.0, nivel LOW")
    void todosEnCero_retornaIrsaCero() {
        IrsaResult result = engine.calculate(0, 0, 0, 0, 0, 0, 0, 0, 0);

        assertEquals(0.0, result.irsaScore(), 0.001);
        assertEquals("LOW", result.riskLevel());
        assertEquals(0.0, result.pollutantScore(), 0.001);
    }

    // ── Normalización de contaminantes ────────────────────────────────────────

    @Test
    @DisplayName("contaminantes exactamente en umbral OMS → normalizados a 1.0, pollutantScore = 1.0")
    void contaminantesEnUmbral_normalizadosA1() {
        // NO2=25, O3=100, PM25=15, UV=6, TMP=35 → cada norm = 1.0
        IrsaResult result = engine.calculate(25, 100, 15, 6, 35, 0, 0, 0, 0);

        assertEquals(1.0, result.normNo2(),  0.001);
        assertEquals(1.0, result.normO3(),   0.001);
        assertEquals(1.0, result.normPm25(), 0.001);
        assertEquals(1.0, result.normUv(),   0.001);
        assertEquals(1.0, result.normTmp(),  0.001);
        assertEquals(1.0, result.pollutantScore(), 0.001);
    }

    @Test
    @DisplayName("contaminantes doble del umbral → norma clampeada a 1.0 (no supera 1)")
    void contaminantesDobleDelUmbral_clampA1() {
        // NO2=50 (2×), O3=200 (2×), PM25=30 (2×), UV=12 (2×), TMP=70 (2×)
        IrsaResult result = engine.calculate(50, 200, 30, 12, 70, 0, 0, 0, 0);

        assertEquals(1.0, result.normNo2(),  0.001);
        assertEquals(1.0, result.normO3(),   0.001);
        assertEquals(1.0, result.normPm25(), 0.001);
        assertEquals(1.0, result.normUv(),   0.001);
        assertEquals(1.0, result.normTmp(),  0.001);
    }

    @Test
    @DisplayName("solo NO2 en umbral → normNo2 = 1.0, pollutantScore = W_NO2 × 1.0 = 0.35")
    void soloNO2EnUmbral_pollutantScoreIgualAlPesoNO2() {
        IrsaResult result = engine.calculate(25, 0, 0, 0, 0, 0, 0, 0, 0);

        // norm = valor / umbral = 25 / 25 = 1.0
        assertEquals(1.0,  result.normNo2(),       0.001);
        assertEquals(0.0,  result.normO3(),        0.001);
        // pollutantScore = W_NO2 * normNo2 = 0.35 * 1.0 = 0.35
        assertEquals(0.35, result.pollutantScore(), 0.001);
    }

    @Test
    @DisplayName("contaminantes a la mitad del umbral → pollutantScore = 0.5")
    void contaminantesEnMitadDeUmbral_pollutantScore05() {
        // NO2=12.5, O3=50, PM25=7.5, UV=3, TMP=17.5
        IrsaResult result = engine.calculate(12.5, 50, 7.5, 3, 17.5, 0, 0, 0, 0);

        assertEquals(0.5, result.normNo2(),       0.001);
        assertEquals(0.5, result.pollutantScore(), 0.001);
    }

    // ── Pesos de contaminantes ────────────────────────────────────────────────

    @Test
    @DisplayName("pollutantScore = suma ponderada de normas: 0.35+0.30+0.20+0.08+0.07 = 1.0")
    void pesosDeContaminantesSumanUno() {
        IrsaResult result = engine.calculate(
                IrsaWeightConfig.THRESHOLD_NO2,
                IrsaWeightConfig.THRESHOLD_O3,
                IrsaWeightConfig.THRESHOLD_PM25,
                IrsaWeightConfig.THRESHOLD_UV,
                IrsaWeightConfig.THRESHOLD_TMP,
                0, 0, 0, 0);

        assertEquals(1.0, result.pollutantScore(), 0.001);
    }

    // ── Factor de vulnerabilidad ──────────────────────────────────────────────

    @Test
    @DisplayName("sin condiciones de salud → vulnerabilityFactor = 1.0")
    void sinSalud_vulnerabilityFactorEs1() {
        IrsaResult result = engine.calculate(0, 0, 0, 0, 0, 0, 0, 0, 0);

        assertEquals(1.0, result.vulnerabilityFactor(), 0.001);
        assertEquals(0.0, result.prevCopd(),      0.001);
        assertEquals(0.0, result.prevAsthma(),    0.001);
        assertEquals(0.0, result.prevPneumonia(), 0.001);
        assertEquals(0.0, result.prevSmoking(),   0.001);
    }

    @Test
    @DisplayName("100% COPD → vulnerabilityFactor = 1.0 + W_COPD = 1.35")
    void todaLaCargaEnCOPD_factorCorrecto() {
        // copdCount=100, resto=0 → prevCopd = 1.0
        IrsaResult result = engine.calculate(0, 0, 0, 0, 0, 100, 0, 0, 0);

        assertEquals(1.0,  result.prevCopd(),           0.001);
        assertEquals(0.0,  result.prevAsthma(),         0.001);
        assertEquals(1.35, result.vulnerabilityFactor(), 0.001);
    }

    @Test
    @DisplayName("distribución igual de enfermedades (25 c/u) → vulnerabilityFactor = 1.25")
    void distribucionIgualEnfermedades_factor125() {
        // 25 cada una → total=100, cada prev = 0.25
        // FV = 1 + 0.35*0.25 + 0.30*0.25 + 0.25*0.25 + 0.10*0.25 = 1.25
        IrsaResult result = engine.calculate(0, 0, 0, 0, 0, 25, 25, 25, 25);

        assertEquals(1.25, result.vulnerabilityFactor(), 0.001);
    }

    @Test
    @DisplayName("100% tabaquismo → vulnerabilityFactor = 1.0 + W_SMOKING = 1.10")
    void todaLaCargaEnTabaquismo_factor110() {
        IrsaResult result = engine.calculate(0, 0, 0, 0, 0, 0, 0, 0, 100);

        assertEquals(1.0,  result.prevSmoking(),         0.001);
        assertEquals(1.10, result.vulnerabilityFactor(), 0.001);
    }

    // ── Fórmula final: irsaScore ──────────────────────────────────────────────

    @Test
    @DisplayName("contaminantes al umbral sin salud → IRSA = 50.0 (MODERATE)")
    void contaminantesEnUmbralSinSalud_irsa50() {
        // pollutantScore=1.0, FV=1.0 → 1.0 * (1.0/2.0) * 100 = 50.0
        IrsaResult result = engine.calculate(25, 100, 15, 6, 35, 0, 0, 0, 0);

        assertEquals(50.0, result.irsaScore(), 0.001);
        assertEquals("MODERATE", result.riskLevel());
    }

    @Test
    @DisplayName("mitad del umbral sin salud → IRSA = 25.0 (LOW)")
    void mitadDelUmbralSinSalud_irsa25() {
        // pollutantScore=0.5, FV=1.0 → 0.5 * 0.5 * 100 = 25.0
        IrsaResult result = engine.calculate(12.5, 50, 7.5, 3, 17.5, 0, 0, 0, 0);

        assertEquals(25.0, result.irsaScore(), 0.001);
        assertEquals("LOW", result.riskLevel());
    }

    @Test
    @DisplayName("contaminantes al umbral + distribución igual de enfermedades → IRSA = 62.5 (MODERATE)")
    void umbralConDistribucionIgual_irsa625() {
        // pollutantScore=1.0, FV=1.25 → 1.0 * (1.25/2.0) * 100 = 62.5
        IrsaResult result = engine.calculate(25, 100, 15, 6, 35, 25, 25, 25, 25);

        assertEquals(62.5, result.irsaScore(), 0.001);
        assertEquals("MODERATE", result.riskLevel());
    }

    @Test
    @DisplayName("IRSA nunca supera 100 aunque los parámetros sean extremos")
    void irsaNuncaSuperaCien() {
        IrsaResult result = engine.calculate(9999, 9999, 9999, 9999, 9999, 99999, 99999, 99999, 99999);

        assertTrue(result.irsaScore() <= 100.0, "IRSA no debe superar 100");
    }

    @Test
    @DisplayName("IRSA nunca baja de 0 con entradas negativas")
    void irsaNuncaBajaDeCero() {
        IrsaResult result = engine.calculate(-10, -50, -5, -3, -20, 0, 0, 0, 0);

        assertTrue(result.irsaScore() >= 0.0, "IRSA no debe ser negativo");
        assertEquals(0.0, result.irsaScore(), 0.001);
    }

    // ── Metadatos del resultado ───────────────────────────────────────────────

    @Test
    @DisplayName("calculatedAt del resultado no es null")
    void calculatedAt_noEsNull() {
        IrsaResult result = engine.calculate(0, 0, 0, 0, 0, 0, 0, 0, 0);

        assertNotNull(result.calculatedAt());
    }

    @Test
    @DisplayName("valores normalizados tienen máximo 4 decimales (round4)")
    void valoresRedondeadosA4Decimales() {
        // 10/25 = 0.4 exacto — verificar que no haya artefactos de coma flotante
        IrsaResult result = engine.calculate(10, 100, 15, 6, 35, 0, 0, 0, 0);

        assertEquals(0.4, result.normNo2(), 0.00001);
    }
}
