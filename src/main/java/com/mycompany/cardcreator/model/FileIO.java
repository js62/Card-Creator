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
 * Handles saving and loading project data to/from json files.
 */
public class FileIO {

    // saves the entire model to data.json in the project folder
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
     * Loads a Model from the data.json in the given folder.
     * Returns null if file doesnt exist or cant be read.
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
            model.setBackgroundImagePath(sm.backgroundImagePath);
            model.setImgX(sm.imgX);
            model.setImgY(sm.imgY);
            model.setImgW(sm.imgW);
            model.setImgH(sm.imgH);

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

    // creates a brand new empty project
    public static void createProject(Path projectFolder) {
        Model model = new Model();
        model.setFolder(projectFolder);
        saveModel(model);
    }
}


// wrapper classes for gson serialization below

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

class SavableCard {
    String id;
    int width;
    int height;
    List<SavableElement> elements;
}

class SavableModel {

    String folder;
    int pageWidth;
    int pageHeight;
    int cardWidth;
    int cardHeight;
    String backgroundImagePath;
    int imgX, imgY, imgW, imgH;
    List<SavableCard> cards;

    public SavableModel() {}

    public SavableModel(Model m) {
        folder = m.getFolder().toAbsolutePath().toString();
        pageWidth = m.getPageWidth();
        pageHeight = m.getPageHeight();
        cardWidth = m.getCardWidth();
        cardHeight = m.getCardHeight();
        backgroundImagePath = m.getBackgroundImagePath();
        imgX = m.getImgX();
        imgY = m.getImgY();
        imgW = m.getImgW();
        imgH = m.getImgH();

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
