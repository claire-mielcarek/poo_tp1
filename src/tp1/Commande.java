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
 * @author clair
 */
public class Commande implements Serializable {

    private String texte;
    private CommandType type;
    private ArrayList<String> arguments = new ArrayList<>();

    public Commande(String commande) {
        texte = commande;
        int nbArgs = 1;
        if (commande != null) {
            texte = new String(commande);
            String[] words = separation(commande);
            switch (words[0]) {
                case ("chargement"):
                    type = CommandType.CHARGEMENT;
                    nbArgs = 1;
                    break;
                case ("compilation"):
                    type = CommandType.COMPILATION;
                    nbArgs = 2;
                    break;
                case ("creation"):
                    type = CommandType.CREATION;
                    nbArgs = 2;
                    break;
                case ("ecriture"):
                    type = CommandType.ECRITURE;
                    nbArgs = 3;
                    break;
                case ("lecture"):
                    type = CommandType.LECTURE;
                    nbArgs = 2;
                    break;
                case ("fonction"):
                    type = CommandType.FONCTION;
                    nbArgs = 3;
                    break;
                default:
                    type = CommandType.ERROR;
            }

            if (words.length != nbArgs + 1) {
                type = CommandType.ERROR;
            }

            for (int i = 1; i < words.length; i++) {
                arguments.add(words[i]);
            }
        } else {
            this.arguments = null;
            type = CommandType.ERROR;
        }
    }

    public CommandType getType() {
        return type;
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    public String getTexte() {
        return texte;
    }

    private String[] separation(String commande) {
        String[] res;
        String[] words = commande.split("#");
        if (commande.endsWith("#")) {
            res = new String[words.length + 1];
            for (int i = 0; i < words.length; i++) {
                res[i] = words[i];
            }
            res[words.length] = "";
        } else {
            res = words;
        }
        return res;
    }

}
