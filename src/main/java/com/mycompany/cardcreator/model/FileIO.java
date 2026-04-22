package com.mycompany.cardcreator.model;

import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Saves and loads projects as json.
 *
 * A project is stored in its own folder, with the data in a file called
 * data.json. Save writes a copy of the Model; load reads it back and
 * rebuilds a Model from the saved state.
 */
public class FileIO {

    /**
     * Writes the whole Model to data.json in its project folder.
     *
     * A failure to write (disk full, permission denied) is printed to
     * stdout and swallowed; the caller is not told the save failed.
     *
     * @param model the Model to save; must have a project folder set
     */
    public static void saveModel(Model model) {
        Path dataFile = model.getFolder().resolve("data.json");
        SavableModel m = new SavableModel(model);
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(m);

        try {
            Files.writeString(dataFile, json);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Loads a Model from data.json in the given folder.
     *
     * Returns null if the file is missing or cannot be read. A returned
     * Model already has its folder set and every card and element put
     * back under the ids they had when saved.
     *
     * @param projectFolder the project folder to read from
     * @return the loaded Model, or null on failure
     */
    public static Model loadModel(Path projectFolder) {
        Path dataFile = projectFolder.resolve("data.json");
        if (!Files.exists(dataFile)) {
            System.out.println("No data.json found in " + projectFolder);
            return null;
        }
        try {
            String json = Files.readString(dataFile);
            SavableModel sm = new Gson().fromJson(json, SavableModel.class);

            Model model = new Model();
            model.setFolder(projectFolder);

            // rebuild cards and their elements from saved data
            if (sm.cards != null) {
                for (SavableCard sc : sm.cards) {
                    UUID cardID = UUID.fromString(sc.id);
                    model.restoreCard(cardID, sc.width, sc.height);

                    if (sc.elements != null) {
                        for (SavableElement se : sc.elements) {
                            CardElement el = new CardElement(
                                CardElementType.valueOf(se.type),
                                se.x, se.y, se.width, se.height
                            );
                            el.text = se.text;
                            el.fontSize = se.fontSize;
                            el.colorHex = se.colorHex;
                            el.filled = se.filled;
                            el.rotation = se.rotation;
                            el.imagePath = se.imagePath;
                            el.zLayer = se.zLayer;
                            model.addCardElement(cardID, el);
                        }
                    }
                }
            }

            return model;
        } catch (IOException ex) {
            System.out.println("Error loading project: " + ex);
            return null;
        }
    }

    /**
     * Creates a brand new empty project in the given folder.
     *
     * A fresh Model is made with that folder as its root and then saved
     * straight away, so data.json exists before the user opens the editor.
     *
     * @param projectFolder the empty folder to put the project in
     */
    public static void createProject(Path projectFolder) {
        Model model = new Model();
        model.setFolder(projectFolder);
        saveModel(model);
    }
}


// wrapper classes for gson serialization below

/**
 * Plain fields copy of CardElement used when reading and writing json.
 *
 * Kept separate from CardElement so the serialized shape does not change
 * if CardElement grows helper methods or AWT types.
 */
class SavableElement {
    String type;
    int x, y, width, height;
    String text;
    int fontSize;
    String colorHex;
    boolean filled;
    double rotation;
    String imagePath;
    int zLayer;
}

/**
 * Plain fields copy of Card used when reading and writing json.
 *
 * The id is stored as a string because gson does not serialize UUID by
 * default.
 */
class SavableCard {
    String id;
    int width;
    int height;
    List<SavableElement> elements;
}

/**
 * Plain fields copy of Model used when reading and writing json.
 *
 * Holds everything worth saving: page size, card size, and every card
 * with its elements.
 */
class SavableModel {

    String folder;
    int pageWidth;
    int pageHeight;
    int cardWidth;
    int cardHeight;
    List<SavableCard> cards;

    /** No-arg constructor used by gson on load. */
    public SavableModel() {}

    /**
     * Copies every saved field out of the given Model into this wrapper.
     *
     * @param m the Model being saved
     */
    public SavableModel(Model m) {
        folder = m.getFolder().toAbsolutePath().toString();
        pageWidth = m.getPageWidth();
        pageHeight = m.getPageHeight();
        cardWidth = m.getCardWidth();
        cardHeight = m.getCardHeight();

        // serialize each card and its elements
        cards = new ArrayList<>();
        for (UUID cardID : m.getCardIDs()) {
            SavableCard sc = new SavableCard();
            Card card = m.getCard(cardID);
            sc.id = cardID.toString();
            sc.width = card.getWidth();
            sc.height = card.getHeight();

            sc.elements = new ArrayList<>();
            for (UUID elID : card.getElementIDs()) {
                CardElement el = m.getCardElement(elID);
                if (el == null) continue;

                SavableElement se = new SavableElement();
                se.type = el.type.name();
                se.x = el.x;
                se.y = el.y;
                se.width = el.width;
                se.height = el.height;
                se.text = el.text;
                se.fontSize = el.fontSize;
                se.colorHex = el.colorHex;
                se.filled = el.filled;
                se.rotation = el.rotation;
                se.imagePath = el.imagePath;
                se.zLayer = el.zLayer;
                sc.elements.add(se);
            }
            cards.add(sc);
        }
    }
}
