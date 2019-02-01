/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private ApplicationClient() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        
        
    }

    /**
     * prend le fichier contenant la liste des commandes, et le charge dans une
     * variable du type Commande qui est retournée      
     */
    public Commande saisisCommande(BufferedReader fichier) throws IOException {
        String cmd = fichier.readLine();
        return new Commande(cmd);
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
        if (uneCommande.getTexte() != null)
        {
            sortieConnexion.write(uneCommande.getTexte());
            sortieConnexion.flush();
            sortieConnexion.write(caractereArret);
            sortieConnexion.flush();
            sortieConnexion.close();
        }
        //Récupérer le résultat sur entreeConnexion
        //TODO:
        //System.out.println("retour serveur: "+ entreeConnexion.read());
        
        Object object = new Object(){
            public String toString() {
                String resultat =  super.toString();
                resultat +=  "\tCommande: " ;
                resultat +=  "\tAttribut(s): ";   
                resultat +=  "\tResultat: ";   
                return resultat;  
            }
        };
        //Fermer la connexion
        try {
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            echoSocket = null;
        }
        return object;
    }

    /**
     * cette méthode vous sera fournie plus tard. Elle indiquera la séquence
     * d’étapes à exécuter pour le test. Elle fera des appels successifs à
     * saisisCommande(BufferedReader fichier) et traiteCommande(Commande
     * uneCommande).      
     */
    public void scenario() {
    sortie.println("Debut des traitements:");
    sortie.flush();
    Commande prochaine;
    try {
        prochaine = saisisCommande(commandes);
        while (prochaine.getArguments() != null) {
        sortie.println("\tTraitement de la commande " + prochaine.getTexte() + " ...");
        sortie.flush();
        Object resultat = traiteCommande(prochaine);

        sortie.println("\r\n" + resultat.toString() + "\r\n");
        sortie.flush();
        System.out.println(prochaine.getTexte());
        prochaine = saisisCommande(commandes);
        }
    sortie.println("Fin des traitements");
    sortie.flush();
    sortie.close();
    } catch (IOException ex) {
        Logger.getLogger(ApplicationClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
    
    

    /**
     * programme principal. Prend 4 arguments: 1) “hostname” du serveur, 2)
     * numéro de port, 3) nom fichier commandes, et 4) nom fichier sortie. Cette
     * méthode doit créer une instance de la classe ApplicationClient,
     * l’initialiser, puis exécuter le scénario      
     */
    public static void main(String[] args) {

        try {
            
            ApplicationClient ac = new ApplicationClient();
            //hostname
            ac.hostName = "localhost";
            //numero port
            ac.port = 8080;
            //nom fichier commandes
            //nom fichier sortie
            ac.initialise(new File("src\\tp1\\commandes.txt").getAbsolutePath(), new File("src\\tp1\\sortie.txt").getAbsolutePath());
            System.out.println("test client\n");

            //executer scenario
            //TODO: ordre des applications des méthodes
            Thread thread = new Thread("New Thread") {
                public void run(){ 	
                    ApplicationServeur.main(args);
                }
            }; 
            
            thread.start();
            
            ac.scenario();

            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ApplicationClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
