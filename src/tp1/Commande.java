/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tp1;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Claire & Tiffany
 */
public class Commande implements Serializable {

    private String texte;
    private CommandeType type;
    private ArrayList<String> arguments = new ArrayList<>();

    public Commande(String commande) {
        texte = commande;
        int nbArgs = 1;
        if (commande != null) {
            texte = commande;
            String[] words = separation(commande);
            switch (words[0]) {
                case ("chargement"):
                    type = CommandeType.CHARGEMENT;
                    nbArgs = 1;
                    break;
                case ("compilation"):
                    type = CommandeType.COMPILATION;
                    nbArgs = 2;
                    break;
                case ("creation"):
                    type = CommandeType.CREATION;
                    nbArgs = 2;
                    break;
                case ("ecriture"):
                    type = CommandeType.ECRITURE;
                    nbArgs = 3;
                    break;
                case ("lecture"):
                    type = CommandeType.LECTURE;
                    nbArgs = 2;
                    break;
                case ("fonction"):
                    type = CommandeType.FONCTION;
                    nbArgs = 3;
                    break;
                default:
                    type = CommandeType.ERROR;
            }

            //Le nombre doit être nbArgs plus un, puisqu'il faut le bon nombre
            //d'arguments ET le type de la commande
            if (words.length != nbArgs + 1) {
                type = CommandeType.ERROR;
            }

            for (int i = 1; i < words.length; i++) {
                arguments.add(words[i]);
            }
        } else {
            this.arguments = null;
            type = CommandeType.ERROR;
        }
    }

    public CommandeType getType() {
        return type;
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    public String getTexte() {
        return texte;
    }

    /**
     * Fonction permettant de prendre en compte les commandes de type FONCTION
     * dans lesquelles il n'y a pas d'argument (la méthode split supprime
     * l'argument au lieu de mettre un argument vide)
     *
     * @param commande La commande à parser
     * @return Le tableau des différents strings composant la commande
     */
    private String[] separation(String commande) {
        String[] res;
        String[] words = commande.split("#");
        if (commande.endsWith("#")) {
            res = new String[words.length + 1];
            System.arraycopy(words, 0, res, 0, words.length);
            res[words.length] = "";
        } else {
            res = words;
        }
        return res;
    }

}
