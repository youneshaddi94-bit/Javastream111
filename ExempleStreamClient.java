import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExempleStreamClient {
    static List<Client> clients = new ArrayList<>();

    public void setClients(List<Client> clients) {
        this.clients = clients;
    };
    static List<Client> AjoutClient(){
        List<Client> liste_client = new ArrayList<>();
        liste_client.add(new Client(1,"nom1","v1",30));
        liste_client.add(new Client(2,"nom2","v1",40));
        liste_client.add(new Client(3,"nom3","v1",50));
        liste_client.add(new Client(4,"nom4","v2",100));
        liste_client.add(new Client(5,"nom5","v2",70));
        liste_client.add(new Client(6,"nom6","v2",80));



        return liste_client;
    }
    public static void main(String[] args) {
        clients=AjoutClient();
        //System.out.println(clients);
        System.out.println("Q1: Exemple de filter    :");
        clients.stream()
                .filter(c -> c.getSolde()>50)
                .forEach(System.out::println);


        System.out.println("Q2: Map vers un nouveau type de données    :");
        List<String> liste_noms= clients.stream()
                .map(Client::getNom)
                .toList();
        liste_noms.forEach(System.out::println);

        System.out.println("Q3: Liste des Clients triés selon les noms:");
        clients.stream()
                .sorted(Comparator.comparing(Client::getSolde))
                .forEach(System.out::println);


        Map<String, Double> caMoyenParVille = clients.stream()
                .collect(Collectors.groupingBy(
                        Client::getVille, // Clé de regroupement : adresse (ville)
                        Collectors.averagingDouble(Client::getSolde) // Downstream Collector : calcul de la moyenne
                ));

        System.out.println("\nCA Moyen par Ville :");
        caMoyenParVille.forEach((ville, moyenne) -> System.out.printf("%s : %.2f\n", ville, moyenne));
    }
}
