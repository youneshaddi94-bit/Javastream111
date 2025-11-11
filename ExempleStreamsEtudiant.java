import java.util.*;
import java.util.stream.*;


public class ExempleStreamsEtudiant {

    public static void main(String[] args) {
        // Création d'une liste d'étudiants
        List<Etudiant> etudiants = Arrays.asList(
                new Etudiant(1, "Amina Fathi", "amina.Fathi@etu.ma", 15.5),
                new Etudiant(2, "Youssef Hilmi", "youssef.Hilmi@etu.ma", 12.0),
                new Etudiant(3, "Fatima Zahrae Kettani", "fatima.kettani@etu.ma", 17.8),
                new Etudiant(4, "Mehdi Jabrane", "mehdi.jabrane@etu.ma", 9.5),
                new Etudiant(5, "Salma Najih", "salma.Najih@etu.ma", 14.2),
                new Etudiant(6, "Hamza Farah", "hamza.Farah@etu.ma", 11.7),
                new Etudiant(7, "Zineb Ahamadi", "zineb.ahmadi@etu.ma", 16.3)
        );

        System.out.println("========== EXEMPLES DE STREAMS JAVA ==========\n");

//        // 1. FILTRAGE - Étudiants ayant réussi (moyenne >= 10)
        System.out.println("1. Étudiants ayant réussi (moyenne >= 10) :");
        etudiants.stream()
                .filter(e -> e.getMoyenne() >= 10)  // Filtre les étudiants
                .forEach(System.out::println);       // Affiche chaque étudiant
        System.out.println();
//
//        // 2. MAP - Extraire uniquement les noms des étudiants
        System.out.println("2. Liste des noms :");
        List<String> noms = etudiants.stream()
                .map(Etudiant::getNom)  // Transforme Etudiant en String
                .collect(Collectors.toList());
        System.out.println(noms);
        System.out.println();
//
//        // 3. SORTED - Trier par moyenne décroissante
        System.out.println("3. Étudiants triés par moyenne (décroissant) :");
        etudiants.stream()
                .sorted((e1, e2) -> Double.compare(e2.getMoyenne(), e1.getMoyenne()))
                .forEach(System.out::println);
        System.out.println();
//
//        // 4. COUNT - Compter les étudiants avec mention (moyenne >= 14)
//        System.out.println("4. Nombre d'étudiants avec mention (>= 14) :");
//        long nbMention = etudiants.stream()
//                .filter(e -> e.getMoyenne() >= 14)
//                .count();
//        System.out.println(nbMention + " étudiants");
//        System.out.println();
//
//        // 5. AVERAGE - Calculer la moyenne générale de la classe
//        System.out.println("5. Moyenne générale de la classe :");
//        double moyenneClasse = etudiants.stream()
//                .mapToDouble(Etudiant::getMoyenne)
//                .average()
//                .orElse(0.0);
//        System.out.printf("%.2f/20\n\n", moyenneClasse);
//
//        // 6. MAX/MIN - Meilleur et moins bon étudiant
//        System.out.println("6. Meilleur étudiant :");
//        Optional<Etudiant> meilleur = etudiants.stream()
//                .max(Comparator.comparing(Etudiant::getMoyenne));
//        meilleur.ifPresent(System.out::println);
//        System.out.println();
//
//        // 7. ANYMATCH - Vérifier si au moins un étudiant a échoué
//        System.out.println("7. Y a-t-il des étudiants en échec (< 10) ? :");
//        boolean aEchoue = etudiants.stream()
//                .anyMatch(e -> e.getMoyenne() < 10);
//        System.out.println(aEchoue ? "Oui" : "Non");
//        System.out.println();
//
//        // 8. COLLECT - Grouper par mention
//        System.out.println("8. Étudiants groupés par mention :");
//        Map<String, List<Etudiant>> parMention = etudiants.stream()
//                .collect(Collectors.groupingBy(e -> {
//                    if (e.getMoyenne() >= 16) return "Très Bien";
//                    else if (e.getMoyenne() >= 14) return "Bien";
//                    else if (e.getMoyenne() >= 12) return "Assez Bien";
//                    else if (e.getMoyenne() >= 10) return "Passable";
//                    else return "Échec";
//                }));
//
//        parMention.forEach((mention, liste) -> {
//            System.out.println("  " + mention + " : " + liste.size() + " étudiant(s)");
//            liste.forEach(e -> System.out.println("    - " + e));
//        });
//        System.out.println();
//
//        // 9. LIMIT & SKIP - Top 3 des meilleurs étudiants
//        System.out.println("9. Top 3 des meilleurs étudiants :");
//        etudiants.stream()
//                .sorted((e1, e2) -> Double.compare(e2.getMoyenne(), e1.getMoyenne()))
//                .limit(3)  // Prendre seulement les 3 premiers
//                .forEach(System.out::println);
//        System.out.println();
//
//        // 10. DISTINCT - Extraire les domaines email uniques
//        System.out.println("10. Domaines email uniques :");
//        etudiants.stream()
//                .map(e -> e.getAdresseMail().split("@")[1])  // Extraire le domaine
//                .distinct()  // Garder uniquement les valeurs uniques
//                .forEach(domaine -> System.out.println("  - " + domaine));
//        System.out.println();
//
//        // 11. REDUCE - Calculer la somme des moyennes manuellement
//        System.out.println("11. Somme totale des moyennes (avec reduce) :");
//        double sommeMoyennes = etudiants.stream()
//                .mapToDouble(Etudiant::getMoyenne)
//                .reduce(0, Double::sum);
//        System.out.printf("%.2f\n\n", sommeMoyennes);
//
//        // 12. ALLMATCH - Vérifier si tous les étudiants ont une adresse email
//        System.out.println("12. Tous les étudiants ont-ils une adresse email ? :");
//        boolean tousOntEmail = etudiants.stream()
//                .allMatch(e -> e.getAdresseMail() != null
//                        && !e.getAdresseMail().isEmpty());
//        System.out.println(tousOntEmail ? "Oui" : "Non");
//        System.out.println();
//
//        // 13. PARALLEL STREAM - Traitement parallèle pour grandes listes
//        System.out.println("13. Calcul de la moyenne avec parallel stream :");
//        double moyenneParallele = etudiants.parallelStream()  // Stream parallèle
//                .mapToDouble(Etudiant::getMoyenne)
//                .average()
//                .orElse(0.0);
//        System.out.printf("%.2f/20\n\n", moyenneParallele);
//
//        // 14. COLLECT avec Collectors personnalisés
//        System.out.println("14. Statistiques complètes des moyennes :");
//        DoubleSummaryStatistics stats = etudiants.stream()
//                .collect(Collectors.summarizingDouble(Etudiant::getMoyenne));
//        System.out.println("  Nombre: " + stats.getCount());
//        System.out.printf("  Moyenne: %.2f\n", stats.getAverage());
//        System.out.printf("  Min: %.2f\n", stats.getMin());
//        System.out.printf("  Max: %.2f\n", stats.getMax());
//        System.out.printf("  Somme: %.2f\n", stats.getSum());
    }
}