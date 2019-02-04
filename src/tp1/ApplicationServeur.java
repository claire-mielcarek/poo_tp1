/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tp1;

import java.io.BufferedReader;
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
 * @author Claire & Tiffany
 */
public class ApplicationServeur {

    private final ServerSocket socket;
    private PrintWriter sortie;
    private BufferedReader entree;
    private final ArrayList<Class> classesChargees;
    Hashtable<String, Object> instances;
    private final char caractereArret = '&';

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
            System.out.println("serveur> A vos ordres:");
            Socket clientSocket = socket.accept();
            //envoie le résultat au client
            sortie = new PrintWriter(clientSocket.getOutputStream(), true);
            //récupère la ligne de commande envoyée par le client
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
            //exécution de la commande
            traiteCommande(new Commande(new String(cmdChars)));
            System.out.println("serveur> Ordre exécuté");
        }
    }

    /**
     * prend uneCommande dument formattée, et la traite. Dépendant du type de
     * commande, elle appelle la méthode spécialisée      
     */
    public void traiteCommande(Commande uneCommande) {
        System.out.println("serveur> " + uneCommande.getTexte());
        switch (uneCommande.getType()) {
            case COMPILATION:
                traiterCompilation(uneCommande.getArguments().get(0));
                break;
            case CHARGEMENT:
                traiterChargement(uneCommande.getArguments().get(0));
                break;
            case CREATION:
                traiterCreation(trouverClasse(uneCommande.getArguments().get(0)), uneCommande.getArguments().get(1));
                break;
            case ECRITURE:
                traiterEcriture(instances.get(uneCommande.getArguments().get(0)), uneCommande.getArguments().get(1), uneCommande.getArguments().get(2));
                break;
            case LECTURE:
                traiterLecture(instances.get(uneCommande.getArguments().get(0)), uneCommande.getArguments().get(1));
                break;
            case FONCTION:
                traiterFonction(instances.get(uneCommande.getArguments().get(0)), uneCommande.getArguments().get(1), uneCommande.getArguments().get(2));
                break;
            default:
                envoyerMessageErreur("Commande invalide");
        }
        sortie.write(caractereArret);
        sortie.flush();
    }

    /**
     * Renvoie la classe chargée recherchée
     *
     * @param nom le nom de la classe désirée
     * @return la classe correspondante ou null si celle-ci n'a pas été chargée
     */
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
     * traiterLecture : traite la lecture d’un attribut. Renvoie le résultat par
     * le socket
     */
    public void traiterLecture(Object pointeurObjet, String attribut) {
        try {
            Field champ = pointeurObjet.getClass().getDeclaredField(attribut);
            Object ret;
            if (champ.isAccessible()) {
                ret = champ.get(pointeurObjet);
            } else {
                char[] nomChamp = champ.getName().toCharArray();
                nomChamp[0] = Character.toUpperCase(nomChamp[0]);
                String nomGetter = "get" + new String(nomChamp);
                Method getter = pointeurObjet.getClass().getDeclaredMethod(nomGetter);
                ret = getter.invoke(pointeurObjet);
            }
            envoyerMessageSucces("Le champ " + attribut + " vaut " + ret);
        } catch (NoSuchFieldException | IllegalArgumentException ex) {
            envoyerMessageErreur("Champ inexistant");
        } catch (SecurityException | InvocationTargetException ex) {
            envoyerMessageErreur("Problème lors de la lecture");
        } catch (IllegalAccessException ex) {
            envoyerMessageErreur("Champ inaccessible");
        } catch (NoSuchMethodException ex) {
            envoyerMessageErreur("Lecture impossible");
        }
    }

    /**
     * traiterEcriture : traite l’écriture d’un attribut. Confirme au client que
     * l’écriture s’est faite correctement.      
     */
    public void traiterEcriture(Object pointeurObjet, String attribut, Object valeur) {
        try {
            Field champ = pointeurObjet.getClass().getDeclaredField(attribut);
            int modifieur = champ.getModifiers();
            if (Modifier.isPublic(modifieur)) {
                champ.set(pointeurObjet, valeur);
            } else {
                char[] nomChamp = champ.getName().toCharArray();
                nomChamp[0] = Character.toUpperCase(nomChamp[0]);
                String nomSetter = "set" + new String(nomChamp);
                Method setter = pointeurObjet.getClass().getDeclaredMethod(nomSetter, valeur.getClass());
                setter.invoke(pointeurObjet, valeur);
            }
            envoyerMessageSucces("Attribut modifié");
        } catch (NoSuchFieldException e) {
            envoyerMessageErreur("Attribut inexistant");
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException ex) {
            envoyerMessageErreur("Problème lors de l'écriture");
        } catch (NoSuchMethodException ex) {
            envoyerMessageErreur("Ecriture impossible");
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
            } catch (SecurityException | IllegalArgumentException | InstantiationException | IllegalAccessException e) {
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
        } catch (ClassNotFoundException e) {
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
        for (String fichier : fichiers) {
            compiler(fichier);
        }
    }

    /**
     * Compile un fichier source
     *
     * @param fichier le chemin absolu vers le fichier à compiler
     */
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
        Class[] classes = trouverClasses(types);
        Object ret;
        try {
            Method m = pointeurObjet.getClass().getDeclaredMethod(nomFonction, classes);
            if (types.length == 0) {
                ret = m.invoke(pointeurObjet);
            } else {
                for (Object valeur : valeurs) {
                }
                ret = m.invoke(pointeurObjet, valeurs);
            }
            if (ret == null) {
                envoyerMessageSucces("La méthode a été exécutée");
            } else {
                envoyerMessageSucces("La méthode a renvoyé \"" + ret + "\"");
            }
        } catch (NoSuchMethodException ex) {
            envoyerMessageErreur("Méthode inexistante");
        } catch (SecurityException | InvocationTargetException ex) {
            envoyerMessageErreur("Problème lors de l'exécution");
        } catch (IllegalAccessException ex) {
            envoyerMessageErreur("Méthode innaccessible");
        } catch (IllegalArgumentException ex) {
            envoyerMessageErreur("Argument illégal");
        }
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
            System.out.println("Connexion serveur ...\n");
            as.aVosOrdres();

        } catch (IOException ex) {
            Logger.getLogger(ApplicationServeur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Envoie un message d'erreur au client
     *
     * @param string le message à envoyer
     */
    private void envoyerMessageErreur(String string) {
        sortie.write("\r\n\t\t");
        sortie.flush();
        sortie.write("Erreur : " + string);
        sortie.flush();
        System.out.println("E : " + string);
    }

    /**
     * Envoie un message au client, le message correspondant à une action
     * effectuée
     *
     * @param string le message à envoyer
     */
    private void envoyerMessageSucces(String string) {
        sortie.write("\r\n\t\t");
        sortie.flush();
        sortie.write("Succès : " + string);
        sortie.flush();
        System.out.println("S : " + string);
    }

    /**
     * Parse les arguments de la fonction nomFonction afin de séparer leurs
     * types de leurs valeurs Récupère les valeurs réelles correspondant au
     * string en argument Appelle la méthode traiterAppel avec les bons
     * arguments
     *
     * @param instance : instance sur laquelle la fonction sera appelée
     * @param nomFonction : nom de la fonction à appeler
     * @param arguments : arguments de la fonction à appeler, sous la forme
     * type1:nom1,type2:nom2,...
     */
    private void traiterFonction(Object instance, String nomFonction, String arguments) {
        ArrayList<String> types = new ArrayList<>();
        ArrayList<Object> valeurs = new ArrayList<>();
        String[] argStructure;
        String valeur;
        float f;
        if (arguments.length() != 0) {
            String[] listeArguments = arguments.split(",");
            for (String arg : listeArguments) {
                argStructure = arg.split(":");
                types.add(argStructure[0]);
                valeur = argStructure[1];
                if (valeur.startsWith("ID(")) {
                    String name = valeur.substring(3, valeur.length() - 1);
                    valeurs.add(instances.get(name));
                } else if (argStructure[0].endsWith("float")) {
                    f = Float.valueOf(argStructure[1]);
                    valeurs.add(f);
                } else if (argStructure[0].endsWith("String")) {
                    // Le seul autre type possible est String
                    valeurs.add(argStructure[1]);
                } else {
                    //On ne gère pas les arguments d'autres types
                    envoyerMessageErreur("Type non géré");
                }
            }
        }
        traiterAppel(instance, nomFonction, arrayListVersArray(types), valeurs.toArray());
    }

    /**
     * @param types tableau contenant les noms des classes à chercher
     * @return le tableau des classes correspondants aux noms donnés
     */
    private Class[] trouverClasses(String[] types) {
        Class c;
        Class[] classes = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            //On gère d'abord les types primitifs pris en compte
            switch (types[i]) {
                case "float":
                    classes[i] = float.class;
                    break;
                case "String":
                    classes[i] = String.class;
                    break;
                default:
                    //Puis on cherche parmis les classes chargées
                    c = trouverClasse(types[i]);
                    if (c != null) {
                        classes[i] = c;
                    } else {
                        envoyerMessageErreur("Type inexistant");
                    }
                    break;
            }
        }
        return classes;
    }

    private String[] arrayListVersArray(ArrayList<String> types) {
        String[] ret = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            ret[i] = types.get(i);
        }
        return ret;
    }

}
