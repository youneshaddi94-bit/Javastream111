import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PerformanceComparaison {
    public static void main(String[] args) {
        List<Integer> grandeListe = IntStream.rangeClosed(1, 1_000_000)
                .boxed()
                .collect(Collectors.toList());

        // Stream séquentiel
        long debut = System.currentTimeMillis();
        long sommeSeq = grandeListe.stream()
                .mapToLong(n -> calculComplexe(n))
                .sum();
        long tempsSeq = System.currentTimeMillis() - debut;

        // Parallel Stream
        debut = System.currentTimeMillis();
        long sommePar = grandeListe.parallelStream()
                .mapToLong(n -> calculComplexe(n))
                .sum();
        long tempsPar = System.currentTimeMillis() - debut;

        System.out.println("Séquentiel: " + tempsSeq + "ms");
        System.out.println("Parallèle: " + tempsPar + "ms");
        System.out.println("Gain: " + (tempsSeq / (double) tempsPar) + "x");
    }

    static long calculComplexe(int n) {
        return (long) ((long) Math.pow(n, 2) + Math.sqrt(n));
    }
}