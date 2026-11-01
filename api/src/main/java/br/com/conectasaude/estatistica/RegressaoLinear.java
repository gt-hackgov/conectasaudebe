package br.com.conectasaude.estatistica;

/**
 * Correlação de Pearson e regressão linear simples (mínimos quadrados) entre duas variáveis.
 *
 * Fórmulas (n pares (x, y)):
 *   Sxx = Σ(x - x̄)²   Syy = Σ(y - ȳ)²   Sxy = Σ(x - x̄)(y - ȳ)
 *   r = Sxy / √(Sxx · Syy)            → força e sentido da relação linear (-1 a 1)
 *   b = Sxy / Sxx                     → inclinação: quanto y muda quando x aumenta 1
 *   a = ȳ - b · x̄                     → intercepto
 *   R² = r²                           → fração da variação de y explicada pela reta
 *
 * Classe pura (sem Spring), para poder ser testada isoladamente. Complexidade O(n).
 */
public record RegressaoLinear(int n, double mediaX, double mediaY, double r, double inclinacao, double intercepto) {

    public static RegressaoLinear ajustar(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length) {
            throw new IllegalArgumentException("x e y precisam ter o mesmo tamanho");
        }
        int n = x.length;
        if (n == 0) {
            return new RegressaoLinear(0, 0, 0, 0, 0, 0);
        }
        double somaX = 0;
        double somaY = 0;
        for (int i = 0; i < n; i++) {
            somaX += x[i];
            somaY += y[i];
        }
        double mediaX = somaX / n;
        double mediaY = somaY / n;

        double sxx = 0;
        double syy = 0;
        double sxy = 0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - mediaX;
            double dy = y[i] - mediaY;
            sxx += dx * dx;
            syy += dy * dy;
            sxy += dx * dy;
        }
        // Sem variação em x (ou em y) não existe reta nem correlação definida.
        double inclinacao = sxx == 0 ? 0 : sxy / sxx;
        double r = (sxx == 0 || syy == 0) ? 0 : sxy / Math.sqrt(sxx * syy);
        double intercepto = mediaY - inclinacao * mediaX;
        return new RegressaoLinear(n, mediaX, mediaY, r, inclinacao, intercepto);
    }

    public double r2() {
        return r * r;
    }

    public double prever(double x) {
        return intercepto + inclinacao * x;
    }

    /** Classificação usual em estatística aplicada: |r| < 0,3 fraca; < 0,7 moderada; ≥ 0,7 forte. */
    public String forca() {
        double abs = Math.abs(r);
        if (abs >= 0.7) {
            return "forte";
        }
        if (abs >= 0.3) {
            return "moderada";
        }
        return "fraca";
    }

    public String sentido() {
        if (Math.abs(r) < 0.1) {
            return "nula";
        }
        return r > 0 ? "positiva" : "negativa";
    }
}
