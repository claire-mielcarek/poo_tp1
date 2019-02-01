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
        if (commande != null)
        {
            texte = new String(commande);

            String[] words = commande.split("#");
            switch (words[0]) {
                case ("chargement"):
                    type = CommandType.CHARGEMENT;
                    break;
                case ("compilation"):
                    type = CommandType.COMPILATION;
                    break;
                case ("creation"):
                    type = CommandType.CREATION;
                    break;
                case ("ecriture"):
                    type = CommandType.ECRITURE;
                    break;
                case ("lecture"):
                    type = CommandType.LECTURE;
                    break;
                case ("fonction"):
                    type = CommandType.FONCTION;
                    break;
                default:
                    type = CommandType.ERROR;
            }

            if (words.length < 2){
                    type = CommandType.ERROR;            
            }

            for( int i = 1; i < words.length; i++){
                arguments.add(words[i]);
            }
        }
        else
            this.arguments = null;
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
    
}
