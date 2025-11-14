# Les Parallel Streams en Java

## 1. Introduction aux Streams et au Parallélisme

### 1.1 Qu'est-ce qu'un Stream ?

Un Stream en Java est une séquence d'éléments qui supporte différentes opérations pour effectuer des calculs sur ces éléments. Introduits dans Java 8, les Streams permettent une programmation fonctionnelle et déclarative pour traiter des collections de données.

**Exemple de Stream séquentiel :**
```java
List<Integer> nombres = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
int somme = nombres.stream()
    .filter(n -> n % 2 == 0)
    .mapToInt(Integer::intValue)
    .sum();
```

### 1.2 Le Parallel Stream

Un Parallel Stream divise les données en plusieurs segments et les traite simultanément sur différents threads, exploitant ainsi les processeurs multi-cœurs modernes. Cette approche peut significativement améliorer les performances pour les opérations coûteuses sur de grandes collections.

**Création d'un Parallel Stream :**
```java
// Méthode 1 : à partir d'une collection
List<Integer> nombres = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
Stream<Integer> parallelStream = nombres.parallelStream();

// Méthode 2 : conversion d'un stream séquentiel
Stream<Integer> parallelStream2 = nombres.stream().parallel();
```

---

## 2. Fonctionnement Interne des Parallel Streams

### 2.1 Le Framework Fork/Join

Les Parallel Streams utilisent le **ForkJoinPool** commun de Java, qui implémente le pattern Fork/Join :

- **Fork** : divise une tâche en sous-tâches plus petites
- **Join** : combine les résultats des sous-tâches

Par défaut, le nombre de threads utilisés est égal au nombre de cœurs disponibles moins un :
```java
int nombreThreads = Runtime.getRuntime().availableProcessors() - 1;
```

### 2.2 Les Opérations Intermédiaires et Terminales

**Opérations intermédiaires** (lazy - retournent un Stream) :
- `filter()` : filtre les éléments
- `map()` : transforme les éléments
- `flatMap()` : aplatit les structures imbriquées
- `distinct()` : élimine les doublons
- `sorted()` : trie les éléments

**Opérations terminales** (eager - déclenchent le traitement) :
- `forEach()` : itère sur chaque élément
- `collect()` : collecte les résultats dans une collection
- `reduce()` : réduit à une valeur unique
- `count()` : compte les éléments
- `anyMatch()`, `allMatch()`, `noneMatch()` : tests booléens

**Exemple complet :**
```java
List<String> mots = Arrays.asList("Java", "Python", "JavaScript", "C++", "Ruby");

List<String> resultat = mots.parallelStream()
    .filter(mot -> mot.length() > 4)      // Intermédiaire
    .map(String::toUpperCase)              // Intermédiaire
    .sorted()                              // Intermédiaire
    .collect(Collectors.toList());         // Terminale

// Résultat : [JAVASCRIPT, PYTHON]
```

---

## 3. Quand Utiliser les Parallel Streams

### 3.1 Cas d'Usage Favorables

Les Parallel Streams sont bénéfiques lorsque :

1. **Volume de données important** : au moins plusieurs milliers d'éléments
2. **Opérations coûteuses** : calculs complexes sur chaque élément
3. **Indépendance des opérations** : pas de dépendances entre les éléments

**Exemple de bon cas d'usage :**
```java
List<Integer> grandeListeNombres = IntStream.rangeClosed(1, 10_000_000)
    .boxed()
    .collect(Collectors.toList());

// Calcul intensif - idéal pour le parallélisme
long sommeCarres = grandeListeNombres.parallelStream()
    .mapToLong(n -> {
        // Simulation d'un calcul coûteux
        return (long) Math.pow(n, 2);
    })
    .sum();
```

### 3.2 Cas d'Usage Défavorables

**Évitez les Parallel Streams quand :**

1. **Petites collections** : l'overhead du parallélisme dépasse le gain
```java
// Mauvais : trop peu d'éléments
List<Integer> petiteListe = Arrays.asList(1, 2, 3, 4, 5);
petiteListe.parallelStream().forEach(System.out::println); // Inutile
```

2. **Opérations avec état partagé** : risque de conditions de course
```java
// DANGER : État partagé mutable
List<Integer> nombres = Arrays.asList(1, 2, 3, 4, 5);
List<Integer> resultat = new ArrayList<>();

// Incorrect - conditions de course !
nombres.parallelStream().forEach(n -> resultat.add(n * 2));

// Correct - utiliser collect()
List<Integer> resultatCorrect = nombres.parallelStream()
    .map(n -> n * 2)
    .collect(Collectors.toList());
```

3. **Opérations dépendantes de l'ordre**
```java
// Problématique avec parallel
nombres.parallelStream().forEach(System.out::println); // Ordre imprévisible

// Solution : utiliser forEachOrdered()
nombres.parallelStream().forEachOrdered(System.out::println);
```

### 3.3 Comparaison de Performance

```java
public class PerformanceComparison {
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
        return (long) Math.pow(n, 2) + Math.sqrt(n);
    }
}
```

---

## 4. Bonnes Pratiques et Pièges à Éviter

### 4.1 Éviter les Effets de Bord

**Mauvaise pratique :**
```java
List<Integer> nombres = Arrays.asList(1, 2, 3, 4, 5);
int[] somme = {0}; // État mutable partagé

// INCORRECT - Race condition
nombres.parallelStream().forEach(n -> somme[0] += n);
```

**Bonne pratique :**
```java
// Utiliser reduce() ou collect()
int somme = nombres.parallelStream()
    .reduce(0, Integer::sum);

// Ou avec mapToInt()
int somme2 = nombres.parallelStream()
    .mapToInt(Integer::intValue)
    .sum();
```

### 4.2 Attention aux Opérations Bloquantes

```java
// Éviter les opérations bloquantes dans les Parallel Streams
nombres.parallelStream()
    .forEach(n -> {
        try {
            Thread.sleep(100); // Bloque un thread du pool
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });
```

### 4.3 Configuration du ForkJoinPool

Par défaut, tous les Parallel Streams partagent le même ForkJoinPool. Pour utiliser un pool personnalisé :

```java
ForkJoinPool customPool = new ForkJoinPool(4);

try {
    List<Integer> resultat = customPool.submit(() ->
        nombres.parallelStream()
            .map(n -> n * 2)
            .collect(Collectors.toList())
    ).get();
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
} finally {
    customPool.shutdown();
}
```

### 4.4 Opérations Stateless vs Stateful

**Stateless (recommandé)** : chaque élément est traité indépendamment
```java
nombres.parallelStream()
    .map(n -> n * 2)           // Stateless
    .filter(n -> n > 10)       // Stateless
    .collect(Collectors.toList());
```

**Stateful (à éviter en parallèle)** : dépend d'éléments précédents
```java
nombres.parallelStream()
    .sorted()                  // Stateful - nécessite de voir tous les éléments
    .distinct()                // Stateful
    .limit(10)                 // Stateful - dépend de l'ordre
    .collect(Collectors.toList());
```

### 4.5 Collectors Thread-Safe

Utilisez des Collectors appropriés pour le parallélisme :

```java
// Thread-safe collectors
Map<Boolean, List<Integer>> partition = nombres.parallelStream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));

Map<Integer, Long> comptage = nombres.parallelStream()
    .collect(Collectors.groupingBy(
        n -> n % 3,
        Collectors.counting()
    ));

// Collectors.toConcurrentMap() pour les Maps concurrentes
ConcurrentMap<Integer, String> map = nombres.parallelStream()
    .collect(Collectors.toConcurrentMap(
        n -> n,
        n -> "Valeur: " + n
    ));
```

### 4.6 Checklist de Décision

Avant d'utiliser un Parallel Stream, vérifiez :

- [ ] La collection contient plus de 1000 éléments
- [ ] Les opérations sont coûteuses en temps CPU
- [ ] Aucun état partagé mutable n'est utilisé
- [ ] L'ordre de traitement n'est pas critique
- [ ] Les opérations sont indépendantes entre elles
- [ ] Aucune opération bloquante (I/O, Thread.sleep)
- [ ] Les tests de performance montrent un gain réel

**Exemple de décision :**
```java
// Bon candidat pour parallelStream()
List<String> fichiers = getListeDeFichiers(); // 10000+ fichiers
List<Document> documents = fichiers.parallelStream()
    .map(fichier -> parseDocument(fichier))    // Opération coûteuse
    .filter(doc -> doc.isValid())
    .collect(Collectors.toList());

// Mauvais candidat pour parallelStream()
List<Integer> nombres = Arrays.asList(1, 2, 3, 4, 5);
int somme = nombres.stream()  // Séquentiel suffit
    .mapToInt(Integer::intValue)
    .sum();
```

---

---

## 5. Exemple Pratique Détaillé : Analyse de Clients

### 5.1 Contexte et Structure

Dans cet exemple, nous utilisons une classe `Client` avec les attributs suivants :
```java
public class Client {
    private final int idclient;
    private final String nom;
    private final String ville;
    private final double solde;
}
```

Nous créons **100 000 clients** répartis dans 7 villes différentes avec des soldes aléatoires entre 0€ et 10 000€. Cette volumétrie importante permet de démontrer l'intérêt réel des Parallel Streams.

### 5.2 Exemple 1 : Filtrage Simple

**Objectif** : Trouver tous les clients ayant un solde supérieur à 5000€

```java
// Stream séquentiel
List<Client> clientsSoldeEleveSeq = clients.stream()
    .filter(c -> c.getSolde() > 5000)
    .collect(Collectors.toList());

// Parallel Stream
List<Client> clientsSoldeElevePar = clients.parallelStream()
    .filter(c -> c.getSolde() > 5000)
    .collect(Collectors.toList());
```

**Analyse** :
- Le filtrage est une opération **stateless** (chaque élément est traité indépendamment)
- Sur 100 000 éléments, le gain n'est pas spectaculaire car l'opération est simple
- Résultat attendu : ~50 000 clients (environ la moitié)

### 5.3 Exemple 2 : Map avec Calcul Coûteux

**Objectif** : Appliquer un calcul complexe sur chaque solde

```java
static double calculComplexe(double solde) {
    double result = solde;
    for (int i = 0; i < 1000; i++) {
        result = Math.sqrt(result + i) * Math.log(result + 1);
    }
    return result;
}

// Parallel Stream avec calcul intensif
List<Double> resultsPar = clients.parallelStream()
    .map(Client::getSolde)
    .map(ExempleParallelStreamClient::calculComplexe)
    .collect(Collectors.toList());
```

**Analyse** :
- C'est le **cas idéal** pour le parallélisme : opération coûteuse en CPU
- Gain typique : **3x à 4x** sur une machine 4 cœurs
- Chaque thread traite un segment de la liste indépendamment
- Le temps d'overhead du parallélisme est négligeable face au temps de calcul

### 5.4 Exemple 3 : GroupBy Concurrent

**Objectif** : Calculer le CA moyen par ville

```java
// Version parallèle avec groupingByConcurrent
Map<String, Double> caMoyenParVille = clients.parallelStream()
    .collect(Collectors.groupingByConcurrent(
        Client::getVille,
        Collectors.averagingDouble(Client::getSolde)
    ));
```

**Points clés** :
- Utilisation de `groupingByConcurrent()` au lieu de `groupingBy()` pour thread-safety
- Retourne une `ConcurrentHashMap` au lieu d'une `HashMap` normale
- Le downstream collector `averagingDouble()` est thread-safe
- Résultat : 7 villes avec leurs moyennes respectives

**Pourquoi concurrent ?**
```java
// ❌ Non thread-safe en parallèle
Collectors.groupingBy(Client::getVille, ...)

// ✅ Thread-safe en parallèle
Collectors.groupingByConcurrent(Client::getVille, ...)
```

### 5.5 Exemple 4 : Statistiques Globales

**Objectif** : Obtenir min, max, moyenne, somme en une seule passe

```java
DoubleSummaryStatistics stats = clients.parallelStream()
    .mapToDouble(Client::getSolde)
    .summaryStatistics();

System.out.println("Nombre : " + stats.getCount());
System.out.println("Min : " + stats.getMin());
System.out.println("Max : " + stats.getMax());
System.out.println("Moyenne : " + stats.getAverage());
System.out.println("Somme : " + stats.getSum());
```

**Analyse** :
- `DoubleSummaryStatistics` est **thread-safe** et optimisé pour le parallélisme
- Évite de parcourir la collection 5 fois (une par statistique)
- Les résultats partiels de chaque thread sont combinés automatiquement

### 5.6 Exemple 5 : Partitionnement VIP vs Standard

**Objectif** : Séparer les clients en deux catégories selon leur solde

```java
Map<Boolean, List<Client>> partition = clients.parallelStream()
    .collect(Collectors.partitioningBy(c -> c.getSolde() > 7000));

List<Client> clientsVIP = partition.get(true);
List<Client> clientsStandard = partition.get(false);
```

**Caractéristiques** :
- `partitioningBy()` crée toujours **exactement 2 groupes** (true/false)
- Différent de `groupingBy()` qui peut créer N groupes
- Thread-safe par défaut, fonctionne bien en parallèle
- Utile pour segmentation client, filtrage A/B, etc.

### 5.7 Exemple 6 : ForkJoinPool Personnalisé

**Objectif** : Contrôler le nombre de threads utilisés

```java
ForkJoinPool customPool = new ForkJoinPool(4);
try {
    double sommeTotale = customPool.submit(() ->
        clients.parallelStream()
            .mapToDouble(Client::getSolde)
            .sum()
    ).get();
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
} finally {
    customPool.shutdown();
}
```

**Quand l'utiliser ?**
- Pour **isoler** un traitement du pool commun
- Pour **limiter** les threads (éviter surcharge CPU)
- Pour des **traitements longs** qui ne doivent pas bloquer les autres

**Attention** :
- Toujours appeler `shutdown()` dans un bloc `finally`
- Gérer les exceptions `InterruptedException` et `ExecutionException`
- Le pool par défaut est généralement suffisant

### 5.8 Exemple 7 : Reduce pour Somme Totale

**Objectif** : Additionner tous les soldes

```java
// Version 1 : avec reduce()
double somme = clients.parallelStream()
    .map(Client::getSolde)
    .reduce(0.0, Double::sum);

// Version 2 : avec mapToDouble() (plus efficace)
double somme2 = clients.parallelStream()
    .mapToDouble(Client::getSolde)
    .sum();
```

**Comparaison** :
- `mapToDouble().sum()` est **plus performant** (évite le boxing/unboxing)
- `reduce()` est plus **flexible** (permet des opérations personnalisées)
- Les deux sont thread-safe et combinables automatiquement

**Fonctionnement du reduce en parallèle** :
```
Thread 1 : [1000, 2000, 3000] → 6000
Thread 2 : [4000, 5000, 6000] → 15000
Thread 3 : [7000, 8000, 9000] → 24000
→ Combine : 6000 + 15000 + 24000 = 45000
```

### 5.9 Exemple 8 : Prédicats Booléens

**Objectif** : Vérifications rapides sur toute la collection

```java
// Existe-t-il au moins un client avec solde négatif ?
boolean existeNegatif = clients.parallelStream()
    .anyMatch(c -> c.getSolde() < 0);

// Tous les clients ont-ils un solde positif ?
boolean tousPositifs = clients.parallelStream()
    .allMatch(c -> c.getSolde() >= 0);

// Aucun client n'a un solde > 1M€ ?
boolean aucunMillionnaire = clients.parallelStream()
    .noneMatch(c -> c.getSolde() > 1_000_000);
```

**Optimisation** :
- Ces opérations peuvent **s'arrêter dès qu'une réponse est trouvée**
- `anyMatch()` s'arrête au premier `true`
- `allMatch()` s'arrête au premier `false`
- En parallèle, plusieurs threads cherchent simultanément → plus rapide

### 5.10 Exemple 9 : Comptage par Ville

**Objectif** : Compter combien de clients par ville

```java
Map<String, Long> clientsParVille = clients.parallelStream()
    .collect(Collectors.groupingByConcurrent(
        Client::getVille,
        Collectors.counting()
    ));

// Affichage trié par nombre décroissant
clientsParVille.entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .forEach(entry -> System.out.printf("%s : %d clients\n",
        entry.getKey(), entry.getValue()));
```

**Points techniques** :
- `Collectors.counting()` est un downstream collector thread-safe
- `groupingByConcurrent()` retourne une `ConcurrentHashMap`
- Le tri final est **séquentiel** (sur seulement 7 entrées)

### 5.11 Comparaison des Performances

**Résultats typiques sur 100 000 clients (machine 8 cœurs)** :

| Opération | Séquentiel | Parallèle | Gain |
|-----------|------------|-----------|------|
| Filtrage simple | 15 ms | 8 ms | 1.9x |
| Calcul complexe | 3200 ms | 450 ms | 7.1x |
| GroupBy | 45 ms | 18 ms | 2.5x |
| Statistiques | 12 ms | 5 ms | 2.4x |
| Reduce/Sum | 10 ms | 4 ms | 2.5x |

**Observations** :
- Le gain est **proportionnel** au coût de l'opération
- Opérations simples : gain modeste (overhead du parallélisme)
- Opérations coûteuses : gain significatif (jusqu'à 7x)
- Le nombre de cœurs limite le gain maximal théorique

### 5.12 Leçons Apprises

✅ **Utilisez Parallel Streams pour** :
- Grandes collections (> 10 000 éléments)
- Calculs intensifs (transformations complexes)
- Opérations stateless et indépendantes

❌ **Évitez Parallel Streams pour** :
- Petites collections (< 1000 éléments)
- Opérations simples (comparaisons, accès direct)
- Code avec état partagé mutable
- Opérations dépendantes de l'ordre

🔍 **Toujours mesurer** :
- Utilisez `System.currentTimeMillis()` ou `System.nanoTime()`
- Comparez séquentiel vs parallèle sur vos données réelles
- Le gain théorique n'est pas toujours le gain pratique

---

## Conclusion

Les Parallel Streams sont un outil puissant pour améliorer les performances des traitements de collections en Java. Cependant, leur utilisation nécessite une compréhension approfondie de leurs mécanismes et limitations. Testez toujours les performances réelles avant d'opter pour le parallélisme, car l'overhead peut annuler les gains sur de petites collections ou des opérations simples.