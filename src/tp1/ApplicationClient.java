/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tp1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author clair
 */
public class ApplicationClient {

    PrintWriter sortie;
    BufferedReader commandes;
    String hostName;
    int port;
    private char caractereArret = '&';

    public ApplicationClient(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    /**
     * prend le fichier contenant la liste des commandes, et le charge dans une
     * variable du type Commande qui est retournée      
     */
    public Commande saisisCommande(BufferedReader fichier) throws IOException {
        return new Commande(fichier.readLine());
    }

    /**
     * initialise : ouvre les différents fichiers de lecture et écriture      
     */
    public void initialise(String fichCommandes, String fichSortie) throws FileNotFoundException {
        try {
            sortie = new PrintWriter(new FileWriter(fichSortie));
        } catch (Exception ec) {
            ec.printStackTrace();
        }

        commandes = new BufferedReader(new FileReader(fichCommandes));
    }

    /**
     * prend une Commande dûment formatée, et la fait exécuter par le serveur.
     * Le résultat de l’exécution est retournée. Si la commande ne retourne pas
     * de résultat, on retourne null. Chaque appel doit ouvrir une connexion,
     * exécuter, et fermer la connexion. Si vous le souhaitez, vous pourriez
     * écrire six fonctions spécialisées, une par type de commande décrit plus
     * haut, qui seront appelées par traiteCommande(Commande uneCommande)      
     */
    public Object traiteCommande(Commande uneCommande) throws IOException {

        Socket echoSocket = new Socket(hostName, port);
        PrintWriter sortieConnexion = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader entreeConnexion = new BufferedReader(
                new InputStreamReader(echoSocket.getInputStream()));
        sortieConnexion.write(uneCommande.getTexte());
        sortieConnexion.write(caractereArret);
        //Récupérer le résultat sur entreeConnexion
        //Fermer la connexion
        return new Object();
    }

    /**
     * cette méthode vous sera fournie plus tard. Elle indiquera la séquence
     * d’étapes à exécuter pour le test. Elle fera des appels successifs à
     * saisisCommande(BufferedReader fichier) et traiteCommande(Commande
     * uneCommande).      
     */
    public void scenario() {

    }

    /**
     * programme principal. Prend 4 arguments: 1) “hostname” du serveur, 2)
     * numéro de port, 3) nom fichier commandes, et 4) nom fichier sortie. Cette
     * méthode doit créer une instance de la classe ApplicationClient,
     * l’initialiser, puis exécuter le scénario      
     */
    public static void main(String[] args) {

    }
}
