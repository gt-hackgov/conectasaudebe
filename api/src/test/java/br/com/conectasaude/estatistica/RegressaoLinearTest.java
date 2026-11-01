package br.com.conectasaude.estatistica;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegressaoLinearTest {

    private static final double TOLERANCIA = 1e-9;

    @Test
    void retaPerfeitaCrescenteTemRIgualAUm() {
        // y = 2x + 1
        RegressaoLinear reg = RegressaoLinear.ajustar(new double[]{1, 2, 3, 4, 5}, new double[]{3, 5, 7, 9, 11});

        assertEquals(1.0, reg.r(), TOLERANCIA);
        assertEquals(2.0, reg.inclinacao(), TOLERANCIA);
        assertEquals(1.0, reg.intercepto(), TOLERANCIA);
        assertEquals(1.0, reg.r2(), TOLERANCIA);
        assertEquals(21.0, reg.prever(10), TOLERANCIA);
        assertEquals("forte", reg.forca());
        assertEquals("positiva", reg.sentido());
    }

    @Test
    void retaPerfeitaDecrescenteTemRIgualAMenosUm() {
        // y = -3x + 20
        RegressaoLinear reg = RegressaoLinear.ajustar(new double[]{0, 1, 2, 3}, new double[]{20, 17, 14, 11});

        assertEquals(-1.0, reg.r(), TOLERANCIA);
        assertEquals(-3.0, reg.inclinacao(), TOLERANCIA);
        assertEquals("negativa", reg.sentido());
    }

    @Test
    void conjuntoConhecidoConfereComCalculoManual() {
        // x̄ = 3, ȳ = 4; Sxy = 6, Sxx = 10, Syy = 6 → b = 0,6; a = 2,2; r = 6/√60 ≈ 0,7746
        RegressaoLinear reg = RegressaoLinear.ajustar(new double[]{1, 2, 3, 4, 5}, new double[]{2, 4, 5, 4, 5});

        assertEquals(0.6, reg.inclinacao(), TOLERANCIA);
        assertEquals(2.2, reg.intercepto(), TOLERANCIA);
        assertEquals(6 / Math.sqrt(60), reg.r(), TOLERANCIA);
        assertEquals("forte", reg.forca());
    }

    @Test
    void semVariacaoEmXNaoInventaCorrelacao() {
        RegressaoLinear reg = RegressaoLinear.ajustar(new double[]{4, 4, 4}, new double[]{1, 2, 3});

        assertEquals(0.0, reg.r(), TOLERANCIA);
        assertEquals(0.0, reg.inclinacao(), TOLERANCIA);
        assertEquals(2.0, reg.intercepto(), TOLERANCIA);
        assertEquals("fraca", reg.forca());
        assertEquals("nula", reg.sentido());
    }

    @Test
    void tamanhosDiferentesSaoRecusados() {
        assertThrows(IllegalArgumentException.class,
                () -> RegressaoLinear.ajustar(new double[]{1, 2}, new double[]{1}));
    }
}
