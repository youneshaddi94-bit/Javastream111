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

## Conclusion

Les Parallel Streams sont un outil puissant pour améliorer les performances des traitements de collections en Java. Cependant, leur utilisation nécessite une compréhension approfondie de leurs mécanismes et limitations. Testez toujours les performances réelles avant d'opter pour le parallélisme, car l'overhead peut annuler les gains sur de petites collections ou des opérations simples.