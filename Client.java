public class Client {
    private final int idclient;
    private final String nom;
    private final String ville;
    private final double solde;
    public Client(int idclient, String nom, String ville, double solde) {
        this.idclient = idclient;
        this.nom = nom;
        this.solde = solde;
        this.ville= ville;

    };

    public String getVille() {
        return ville;
    }

    public int getIdclient() {
        return idclient;
        };
    public String getNom() {
        return nom;
    };
    public double getSolde() {
        return solde;
    };

    @Override
    public String toString() {
        return "Client{" +
                "idclient=" + idclient +
                ", nom='" + nom + '\'' +
                ", ville=" + ville +
                ", solde=" + solde +
                '}';
    }
}
