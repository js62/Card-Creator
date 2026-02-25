package com.mycompany.cardcreator.Modules;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Model contains all data for project and handles all operations on this data.
 */
public class Model {

    public Model() {
    }

    private File projectFolder = null;

    private LinkedHashMap<UUID, CardElement> cardElements = new LinkedHashMap<>();

    /**
     * Creates a new card element and returns its id
     */
    public UUID addCardElement() {
        UUID id = UUID.randomUUID();

        CardElement cl=new CardElement();
        cardElements.put(id, cl);

        return id;
    }

    private LinkedHashMap<UUID,Card> cards = new LinkedHashMap<>();

    public UUID addCard() {
        UUID id = UUID.randomUUID();

        Card c=new Card();
        cards.put(id, c);

        return id;
    }
    
    
    public void setFolder(File folder) {
        projectFolder = folder;
    }

    public File getFolder() {
        return projectFolder;
    }

}
