package fr.chantapp.db;

import java.util.List;

public class ChantJson {

    public long id;
    public String libelleChant;
    public String auteurChant;
    public String compositeurChant;
    public List<Parole> paroles;

    public ChantJson(long id, String libelleChant, String auteurChant, String compositeurChant, List<Parole> paroles){
        this.id = id;
        this.libelleChant = libelleChant;
        this.auteurChant = auteurChant;
        this.compositeurChant = compositeurChant;
        this.paroles = paroles;
    }
}
