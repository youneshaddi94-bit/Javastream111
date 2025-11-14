import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExempleParallelStreamClient {
    static List<Client> clients = new ArrayList<>();

    // Création d'une grande liste de clients pour démontrer l'utilité du parallélisme
    static List<Client> creerGrandeListeClients() {
        List<Client> listeClients = new ArrayList<>();
        String[] villes = {"Rabat", "CasaBlanca", "Tanger", "Laayoune", "Agadir", "Marrakech", "Settat"};
        Random random = new Random(42); // Seed pour reproductibilité

        // Création de 100 000 clients pour voir l'impact du parallélisme
        for (int i = 1; i <= 100000; i++) {
            String nom = "Client_" + i;
            String ville = villes[random.nextInt(villes.length)];
            double solde = random.nextDouble() * 10000;
            listeClients.add(new Client(i, nom, ville, solde));
        }

        return listeClients;
    }

    // Méthode pour simuler un traitement coûteux
    static double calculComplexe(double solde) {
        // Simulation d'un calcul intensif
        double result = solde;
        for (int i = 0; i < 1000; i++) {
            result = Math.sqrt(result + i) * Math.log(result + 1);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== DÉMONSTRATION DES PARALLEL STREAMS ===\n");

        clients = creerGrandeListeClients();
        System.out.println("Nombre total de clients : " + clients.size());
        System.out.println("Nombre de cœurs disponibles : " + Runtime.getRuntime().availableProcessors());
        System.out.println("\n" + "=".repeat(60) + "\n");

        // ==========================================
        // EXEMPLE 1 : Filter - Comparaison Séquentiel vs Parallèle
        // ==========================================
        System.out.println("EXEMPLE 1 : Filtrage des clients avec solde > 5000");
        System.out.println("-".repeat(60));

        // Stream séquentiel
        long debutSeq = System.currentTimeMillis();
        List<Client> clientsSoldeEleveSeq = clients.stream()
                .filter(c -> c.getSolde() > 5000)
                .collect(Collectors.toList());
        long tempsSeq = System.currentTimeMillis() - debutSeq;

        // Parallel Stream
        long debutPar = System.currentTimeMillis();
        List<Client> clientsSoldeElevePar = clients.parallelStream()
                .filter(c -> c.getSolde() > 5000)
                .collect(Collectors.toList());
        long tempsPar = System.currentTimeMillis() - debutPar;

        System.out.println("Résultats trouvés : " + clientsSoldeEleveSeq.size());
        System.out.println("Temps séquentiel : " + tempsSeq + " ms");
        System.out.println("Temps parallèle : " + tempsPar + " ms");
        System.out.printf("Gain de performance : %.2fx\n", tempsSeq / (double) tempsPar);
        System.out.println();

        // ==========================================
        // EXEMPLE 2 : Map avec Traitement Coûteux
        // ==========================================
        System.out.println("EXEMPLE 2 : Calcul complexe sur les soldes");
        System.out.println("-".repeat(60));

        // Prendre seulement 10000 clients pour ce test (plus rapide)
        List<Client> echantillon = clients.subList(0, 10000);

        // Stream séquentiel avec calcul complexe
        debutSeq = System.currentTimeMillis();
        List<Double> resultsSeq = echantillon.stream()
                .map(Client::getSolde)
                .map(ExempleParallelStreamClient::calculComplexe)
                .collect(Collectors.toList());
        tempsSeq = System.currentTimeMillis() - debutSeq;

        // Parallel Stream avec calcul complexe
        debutPar = System.currentTimeMillis();
        List<Double> resultsPar = echantillon.parallelStream()
                .map(Client::getSolde)
                .map(ExempleParallelStreamClient::calculComplexe)
                .collect(Collectors.toList());
        tempsPar = System.currentTimeMillis() - debutPar;

        System.out.println("Calculs effectués : " + resultsSeq.size());
        System.out.println("Temps séquentiel : " + tempsSeq + " ms");
        System.out.println("Temps parallèle : " + tempsPar + " ms");
        System.out.printf("Gain de performance : %.2fx\n", tempsSeq / (double) tempsPar);
        System.out.println();

        // ==========================================
        // EXEMPLE 3 : GroupBy avec Parallel Stream
        // ==========================================
        System.out.println("EXEMPLE 3 : Regroupement par ville (CA moyen)");
        System.out.println("-".repeat(60));

        // Séquentiel
        debutSeq = System.currentTimeMillis();
        Map<String, Double> caMoyenParVilleSeq = clients.stream()
                .collect(Collectors.groupingBy(
                        Client::getVille,
                        Collectors.averagingDouble(Client::getSolde)
                ));
        tempsSeq = System.currentTimeMillis() - debutSeq;

        // Parallèle avec groupingByConcurrent
        debutPar = System.currentTimeMillis();
        Map<String, Double> caMoyenParVillePar = clients.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                        Client::getVille,
                        Collectors.averagingDouble(Client::getSolde)
                ));
        tempsPar = System.currentTimeMillis() - debutPar;

        System.out.println("Villes analysées : " + caMoyenParVilleSeq.size());
        caMoyenParVillePar.forEach((ville, moyenne) ->
                System.out.printf("  %s : %.2f€\n", ville, moyenne));
        System.out.println("Temps séquentiel : " + tempsSeq + " ms");
        System.out.println("Temps parallèle : " + tempsPar + " ms");
        System.out.printf("Gain de performance : %.2fx\n", tempsSeq / (double) tempsPar);
        System.out.println();

        // ==========================================
        // EXEMPLE 4 : Statistiques Avancées
        // ==========================================
        System.out.println("EXEMPLE 4 : Calcul de statistiques globales");
        System.out.println("-".repeat(60));

        // Parallèle
        debutPar = System.currentTimeMillis();
        DoubleSummaryStatistics stats = clients.parallelStream()
                .mapToDouble(Client::getSolde)
                .summaryStatistics();
        tempsPar = System.currentTimeMillis() - debutPar;

        System.out.println("Statistiques calculées en parallèle :");
        System.out.printf("  Nombre de clients : %d\n", stats.getCount());
        System.out.printf("  Solde minimum : %.2f€\n", stats.getMin());
        System.out.printf("  Solde maximum : %.2f€\n", stats.getMax());
        System.out.printf("  Solde moyen : %.2f€\n", stats.getAverage());
        System.out.printf("  Somme totale : %.2f€\n", stats.getSum());
        System.out.println("Temps d'exécution : " + tempsPar + " ms");
        System.out.println();

        // ==========================================
        // EXEMPLE 5 : Partition des Clients
        // ==========================================
        System.out.println("EXEMPLE 5 : Partition VIP vs Standard (solde > 7000€)");
        System.out.println("-".repeat(60));

        debutPar = System.currentTimeMillis();
        Map<Boolean, List<Client>> partition = clients.parallelStream()
                .collect(Collectors.partitioningBy(c -> c.getSolde() > 7000));
        tempsPar = System.currentTimeMillis() - debutPar;

        System.out.println("Clients VIP (> 7000€) : " + partition.get(true).size());
        System.out.println("Clients Standard : " + partition.get(false).size());
        System.out.println("Temps d'exécution parallèle : " + tempsPar + " ms");
        System.out.println();

        // ==========================================
        // EXEMPLE 6 : Top 10 des Meilleurs Clients
        // ==========================================
        System.out.println("EXEMPLE 6 : Top 10 des clients avec le solde le plus élevé");
        System.out.println("-".repeat(60));

        debutPar = System.currentTimeMillis();
        List<Client> top10 = clients.parallelStream()
                .sorted(Comparator.comparing(Client::getSolde).reversed())
                .limit(10)
                .collect(Collectors.toList());
        tempsPar = System.currentTimeMillis() - debutPar;

        System.out.println("Top 10 des meilleurs clients :");
        top10.forEach(c -> System.out.printf("  %s (%s) : %.2f€\n",
                c.getNom(), c.getVille(), c.getSolde()));
        System.out.println("Temps d'exécution : " + tempsPar + " ms");
        System.out.println();

        // ==========================================
        // EXEMPLE 7 : Utilisation d'un ForkJoinPool Personnalisé
        // ==========================================
        System.out.println("EXEMPLE 7 : ForkJoinPool personnalisé (4 threads)");
        System.out.println("-".repeat(60));

        ForkJoinPool customPool = new ForkJoinPool(4);
        try {
            debutPar = System.currentTimeMillis();
            double sommeTotale = customPool.submit(() ->
                    clients.parallelStream()
                            .mapToDouble(Client::getSolde)
                            .sum()
            ).get();
            tempsPar = System.currentTimeMillis() - debutPar;

            System.out.printf("Somme totale calculée : %.2f€\n", sommeTotale);
            System.out.println("Temps avec pool de 4 threads : " + tempsPar + " ms");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            customPool.shutdown();
        }
        System.out.println();

        // ==========================================
        // EXEMPLE 8 : Reduce - Somme des Soldes
        // ==========================================
        System.out.println("EXEMPLE 8 : Reduce - Somme totale des soldes");
        System.out.println("-".repeat(60));

        // Séquentiel
        debutSeq = System.currentTimeMillis();
        double sommeSeq = clients.stream()
                .map(Client::getSolde)
                .reduce(0.0, Double::sum);
        tempsSeq = System.currentTimeMillis() - debutSeq;

        // Parallèle
        debutPar = System.currentTimeMillis();
        double sommePar = clients.parallelStream()
                .map(Client::getSolde)
                .reduce(0.0, Double::sum);
        tempsPar = System.currentTimeMillis() - debutPar;

        System.out.printf("Somme totale : %.2f€\n", sommePar);
        System.out.println("Temps séquentiel : " + tempsSeq + " ms");
        System.out.println("Temps parallèle : " + tempsPar + " ms");
        System.out.printf("Gain de performance : %.2fx\n", tempsSeq / (double) tempsPar);
        System.out.println();

        // ==========================================
        // EXEMPLE 9 : Vérifications avec anyMatch, allMatch
        // ==========================================
        System.out.println("EXEMPLE 9 : Vérifications booléennes");
        System.out.println("-".repeat(60));

        debutPar = System.currentTimeMillis();
        boolean existeSoldeNegatif = clients.parallelStream()
                .anyMatch(c -> c.getSolde() < 0);

        boolean tousPositifs = clients.parallelStream()
                .allMatch(c -> c.getSolde() >= 0);

        boolean aucunSoldeTresEleve = clients.parallelStream()
                .noneMatch(c -> c.getSolde() > 1_000_000);
        tempsPar = System.currentTimeMillis() - debutPar;

        System.out.println("Existe un solde négatif ? " + existeSoldeNegatif);
        System.out.println("Tous les soldes sont positifs ? " + tousPositifs);
        System.out.println("Aucun solde > 1M€ ? " + aucunSoldeTresEleve);
        System.out.println("Temps d'exécution : " + tempsPar + " ms");
        System.out.println();

        // ==========================================
        // EXEMPLE 10 : Compter les Clients par Ville
        // ==========================================
        System.out.println("EXEMPLE 10 : Nombre de clients par ville");
        System.out.println("-".repeat(60));

        debutPar = System.currentTimeMillis();
        Map<String, Long> clientsParVille = clients.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                        Client::getVille,
                        Collectors.counting()
                ));
        tempsPar = System.currentTimeMillis() - debutPar;

        clientsParVille.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("  %s : %d clients\n",
                        entry.getKey(), entry.getValue()));
        System.out.println("Temps d'exécution : " + tempsPar + " ms");
        System.out.println();

        System.out.println("=".repeat(60));
        System.out.println("FIN DES DÉMONSTRATIONS");
    }
}