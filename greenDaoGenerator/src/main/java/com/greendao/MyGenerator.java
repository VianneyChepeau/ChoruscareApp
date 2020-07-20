package com.greendao;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;

public class MyGenerator {
    public static void main(String[] args) {
        Schema schema = new Schema(2, "fr.chantapp.db");
        schema.enableKeepSectionsByDefault();

        addTables(schema);

        try {
            new DaoGenerator().generateAll(schema,"./app/src/main/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTables(final Schema schema) {
        Entity typeParole = schema.addEntity("TypeParole");
        typeParole.addIdProperty().notNull().unique();
        typeParole.addStringProperty("libelleTypeParole").notNull();

        Entity chant = schema.addEntity("Chant");
        chant.addIdProperty().notNull().unique();
        chant.addStringProperty("libelleChant").notNull();
        chant.addStringProperty("auteurChant").notNull();
        chant.addStringProperty("compositeurChant").notNull();
        chant.addStringProperty("cheminPartitionChant");

        Entity parole = schema.addEntity("Parole");
        parole.addIdProperty().notNull().unique();
        parole.addStringProperty("contenuParole").notNull();
        Property ordreAffichageParole = parole.addLongProperty("ordreAffichageParole").notNull().getProperty();
        Property chantId = parole.addLongProperty("chantIdParole").notNull().getProperty();
        Property typeParoleId = parole.addLongProperty("typeParoleIdParole").notNull().getProperty();
        parole.addToOne(chant, chantId);
        parole.addToOne(typeParole, typeParoleId);

        ToMany chantToParoles = chant.addToMany(parole, chantId);
        chantToParoles.setName("paroles");
        chantToParoles.orderAsc(ordreAffichageParole);
    }
}