/**
 * Classe représentant un étudiant
 */
class Etudiant {
    private int idEtudiant;
    private String nom;
    private String adresseMail;
    private double moyenne;

    // Constructeur
    public Etudiant(int idEtudiant, String nom, String adresseMail, double moyenne) {
        this.idEtudiant = idEtudiant;
        this.nom = nom;
        this.adresseMail = adresseMail;
        this.moyenne = moyenne;
    }

    // Getters
    public int getIdEtudiant() { return idEtudiant; }
    public String getNom() { return nom; }
    public String getAdresseMail() { return adresseMail; }
    public double getMoyenne() { return moyenne; }

    // Setters
    public void setMoyenne(double moyenne) { this.moyenne = moyenne; }

    @Override
    public String toString() {
        return String.format("Etudiant[ID=%d, Nom=%s, Email=%s, Moyenne=%.2f]",
                idEtudiant, nom, adresseMail, moyenne);
    }
}

/**
 * Classe principale démontrant l'utilisation des Streams Java
 */