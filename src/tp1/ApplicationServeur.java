/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 *
 * @author clair
 */
public class ApplicationServeur {
    
    private ServerSocket socket;
    private PrintWriter sortie;
    private BufferedReader entree;
    private ArrayList<Class> classesChargees;
    Hashtable<String, Object> instances;
    private char caractereArret = '&';

    /**
     * prend le numéro de port, crée un SocketServer sur le port      
     */
    public ApplicationServeur(int port) throws IOException {
        socket = new ServerSocket(port);
        classesChargees = new ArrayList<>();
        instances = new Hashtable();
    }

    /**
     * Se met en attente de connexions des clients. Suite aux connexions, elle
     * lit ce qui est envoyé à travers la Socket, recrée l’objet Commande envoyé
     * par le client, et appellera traiterCommande(Commande uneCommande)      
     */
    public void aVosOrdres() throws IOException {
        while (true) {
            System.out.println("\nA vos ordres:\n");
            Socket clientSocket = socket.accept();
            sortie = new PrintWriter(clientSocket.getOutputStream(), true);
            entree = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            char tmp = (char) entree.read();
            StringBuffer cmdChars = new StringBuffer();
            while (tmp != caractereArret) {
                if (tmp != -1) {
                    cmdChars.append(tmp);
                    tmp = (char) entree.read();
                }
            }
            System.out.println("cmdChars: " + cmdChars + "\n");
            traiteCommande(new Commande(new String(cmdChars)));
            //TODO:
            sortie.write(cmdChars + " test");
            sortie.flush();
            sortie.close();
            entree.close();
        }
    }

    /**
     * prend uneCommande dument formattée, et la traite. Dépendant du type de
     * commande, elle appelle la méthode spécialisée      
     */
    public void traiteCommande(Commande uneCommande) {
        System.out.println("traite cmd serveur: " + uneCommande.getTexte());
        switch (uneCommande.getType()) {
            case COMPILATION:
                if (uneCommande.getArguments().size() == 2) {
                    // Le prof n'a pas l'air de gérer le 2ème paramètre ?
                    traiterCompilation(uneCommande.getArguments().get(0));
                } else {
                    envoyerMessageErreur("Commande non traitée (nombre d'arguments)");
                }
                break;
            case CHARGEMENT:
                if (uneCommande.getArguments().size() == 1) {
                    traiterChargement(uneCommande.getArguments().get(0));
                } else {
                    envoyerMessageErreur("Commande non traitée (nombre d'arguments)");
                }
                break;
            case CREATION:
                if (uneCommande.getArguments().size() == 2) {
                    traiterCreation(trouverClasse(uneCommande.getArguments().get(0)), uneCommande.getArguments().get(1));
                } else {
                    envoyerMessageErreur("Commande non traitée (nombre d'arguments)");
                }
                break;
            case ECRITURE:
                if (uneCommande.getArguments().size() == 3) {
                    traiterEcriture(instances.get(uneCommande.getArguments().get(0)), uneCommande.getArguments().get(1), uneCommande.getArguments().get(2));
                } else {
                    envoyerMessageErreur("Commande non traitée (nombre d'arguments)");
                }
                break;
            default:
            /*
                    sortie = new PrintWriter(new BufferedWriter(new FileWriter(new File("src\\tp1\\sortie.txt").getAbsolutePath(), true)));
                    sortie.write("commande effectuée\r\n");
                    sortie.flush();
                    sortie.close();
                } catch (IOException ex) {
                    Logger.getLogger(ApplicationServeur.class.getName()).log(Level.SEVERE, null, ex);
                }
             */
        }
    }
    
    private Class trouverClasse(String nom) {
        Class ret = null;
        int i = 0;
        while (i < classesChargees.size() && !nom.equals(classesChargees.get(i).getCanonicalName())) {
            i++;
        }
        if (i != classesChargees.size()) {
            ret = classesChargees.get(i);
        }
        return ret;
    }

    /**
     * traiterLecture : traite la lecture d’un attribut. Renvoies le résultat
     * par le socket      
     */
    public void traiterLecture(Object pointeurObjet, String attribut) {
    }

    /**
     * traiterEcriture : traite l’écriture d’un attribut. Confirmes au client
     * que l’écriture s’est faite correctement.      
     */
    public void traiterEcriture(Object pointeurObjet, String attribut, Object valeur) {
        try {
            Field champ = pointeurObjet.getClass().getDeclaredField(attribut);
            int modifieur = champ.getModifiers();
            if (Modifier.isPublic(modifieur)) {
                champ.set(pointeurObjet, valeur);
                envoyerMessageSucces("Attribut modifié");
            } else {
                char[] nomChamp = champ.getName().toCharArray();
                nomChamp[0] = Character.toUpperCase(nomChamp[0]);
                String nomSetter = "set" + new String(nomChamp);
                Method setter = pointeurObjet.getClass().getDeclaredMethod(nomSetter, valeur.getClass());
                setter.invoke(pointeurObjet, valeur);
                envoyerMessageSucces("Attribut modifié");
            }
        } catch (NoSuchFieldException e) {
            envoyerMessageErreur("Attribut inexistant");
        } catch (IllegalArgumentException ex) {
            envoyerMessageErreur("Problème lors de l'écriture");
        } catch (IllegalAccessException ex) {
            envoyerMessageErreur("Problème lors de l'écriture");
        } catch (NoSuchMethodException ex) {
            envoyerMessageErreur("Ecriture impossible");
        } catch (SecurityException ex) {
            envoyerMessageErreur("Problème lors de l'écriture");
        } catch (InvocationTargetException ex) {            
            envoyerMessageErreur("Problème lors de l'écriture");
        }
    }

    /**
     * traiterCreation : traite la création d’un objet. Confirme au client que
     * la création s’est faite correctement.      
     */
    public void traiterCreation(Class classeDeLobjet, String identificateur) {
        if (classeDeLobjet != null) {
            try {
                Object instance = classeDeLobjet.newInstance();
                instances.put(identificateur, instance);
                envoyerMessageSucces("Objet " + identificateur + " créé");
            } catch (SecurityException e) {
                envoyerMessageErreur("L'instanciation a échoué");
            } catch (IllegalArgumentException e) {
                envoyerMessageErreur("L'instanciation a échoué");
            } catch (InstantiationException e) {
                envoyerMessageErreur("L'instanciation a échoué");
            } catch (IllegalAccessException e) {
                envoyerMessageErreur("L'instanciation a échoué");
            }
        } else {
            envoyerMessageErreur("Classe non chargée");
        }
    }

    /**
     * traiterChargement : traite le chargement d’une classe. Confirmes au
     * client que la création s’est faite correctement.      
     */
    public void traiterChargement(String nomQualifie) {
        try {
            Class classe = Class.forName(nomQualifie);
            classesChargees.add(classe);
            envoyerMessageSucces("Classe chargée : " + classe);
        } catch (Exception e) {
            envoyerMessageErreur("Classe non chargée");
        }
    }

    /**
     * traiterCompilation : traite la compilation d’un fichier source java.
     * Confirme au client que la compilation s’est faite correctement. Le
     * fichier source est donné par son chemin relatif par rapport au chemin des
     * fichiers sources.      
     */
    public void traiterCompilation(String cheminRelatifFichierSource) {
        String[] fichiers = cheminRelatifFichierSource.split(",");
        for (int i = 0; i < fichiers.length; i++) {
            compiler(fichiers[i]);
        }
    }
    
    private void compiler(String fichier) {
        
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int res = compiler.run(null, null, null, fichier);
        
        if (res == 0) {
            envoyerMessageSucces("" + fichier + " compilé");
        } else {
            envoyerMessageErreur("Problème à la compilation de " + fichier);
        }
    }

    /**
     * traiterAppel : traite l’appel d’une méthode, en prenant comme argument
     * l’objet sur lequel on effectue l’appel, le nom de la fonction à appeler,
     * un tableau de nom de types des arguments, et un tableau d’arguments pour
     * la fonction. Le résultat de la fonction est renvoyé par le serveur au
     * client (ou le message que tout s’est bien passé)      
     */
    public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types, Object[] valeurs) {
    }

    /**
     * programme principal. Prend 4 arguments: 1) numéro de port, 2) répertoire
     * source, 3) répertoire classes, et 4) nom du fichier de traces (sortie)
     * Cette méthode doit créer une instance de la classe ApplicationServeur,
     * l’initialiser puis appeler aVosOrdres sur cet objet      
     */
    public static void main(String[] args) {
        try {
            ApplicationServeur as = new ApplicationServeur(8080);
            System.out.println("test serveur\n");
            as.aVosOrdres();
            
        } catch (IOException ex) {
            Logger.getLogger(ApplicationServeur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void envoyerMessageErreur(String string) {
        sortie.write("E : " + string);
        sortie.flush();
        System.out.println("E : " + string);
    }
    
    private void envoyerMessageSucces(String string) {
        sortie.write("S : " + string);
        sortie.flush();
        System.out.println("S : " + string);
    }
    
}
