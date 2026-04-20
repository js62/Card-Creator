package com.mycompany.cardcreator.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Model holds all data for a project and manages operations on it.
 */
public class Model {

    public Model() {
    }

    private Path projectFolder = null;

    //pdf output dimensions, divisble by grid size 25
    private int pageWidth = 675;
    private int pageHeight = 1075;

    //dimensions for the individual cards
    private int cardWidth = pageHeight / 4;
    private int cardHeight = pageWidth / 4;

    private String backgroundImagePath = null;

    // image position and size on canvas (saved with project)
    private int imgX = 0;
    private int imgY = 0;
    private int imgW = 0;
    private int imgH = 0;

    // raw stored value (relative to the project folder, persisted to json).
    // package-private so FileIO can round-trip it; callers should use
    // setBackgroundImage / resolveBackgroundImage instead.
    String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    void setBackgroundImagePath(String path) {
        backgroundImagePath = path;
    }

    /**
     * Public setter for the background image. Stores the path relative to the
     * project folder so the project stays portable across machines. Pass null
     * to clear.
     */
    public void setBackgroundImage(Path imageFile) {
        if (imageFile == null) {
            backgroundImagePath = null;
            return;
        }
        if (projectFolder != null && imageFile.isAbsolute()) {
            backgroundImagePath = projectFolder.relativize(imageFile).toString();
        } else {
            backgroundImagePath = imageFile.toString();
        }
    }

    /**
     * Resolves the stored background image path against the project folder.
     * Returns null if no background is set. If the stored value is already
     * absolute (older saves) it's returned as-is.
     */
    public Path resolveBackgroundImage() {
        if (backgroundImagePath == null || projectFolder == null) {
            return null;
        }
        return projectFolder.resolve(backgroundImagePath);
    }

    public int getImgX() {
        return imgX;
    }

    public void setImgX(int x) {
        imgX = x;
    }

    public int getImgY() {
        return imgY;
    }

    public void setImgY(int y) {
        imgY = y;
    }

    public int getImgW() {
        return imgW;
    }

    public void setImgW(int w) {
        imgW = w;
    }

    public int getImgH() {
        return imgH;
    }

    public void setImgH(int h) {
        imgH = h;
    }


    // all elements across all cards, keyed by uuid
    private LinkedHashMap<UUID, CardElement> cardElements = new LinkedHashMap<>();

    /**
     * Creates a new element on a specific card and returns its uuid
     */
    public UUID addCardElement(UUID cardID, CardElement element) {
        UUID id = UUID.randomUUID();
        element.id = id;
        cardElements.put(id, element);

        Card card = cards.get(cardID);
        if(card != null){
            card.addElementID(id);
        }
        return id;
    }

    public CardElement getCardElement(UUID elementID) {
        return cardElements.get(elementID);
    }

    public void removeCardElement(UUID cardID, UUID elementID) {
        cardElements.remove(elementID);

        Card card = cards.get(cardID);
        if (card != null) {
            card.removeElementID(elementID);
        }
    }

    public LinkedHashMap<UUID, CardElement> getCardElements() {
        return cardElements;
    }


    // all cards in the project
    private LinkedHashMap<UUID, Card> cards = new LinkedHashMap<>();

    public Card getCard(UUID CardID) {
        return cards.get(CardID);
    }

    public ArrayList<UUID> getCardIDs() {
        return new ArrayList<>(cards.keySet());
    }

    public UUID addCard() {
        UUID id = UUID.randomUUID();
        Card c = new Card(cardWidth, cardHeight);
        cards.put(id, c);
        return id;
    }

    // used when loading from json, card id is already known
    public void restoreCard(UUID id, int width, int height) {
        cards.put(id, new Card(width, height));
    }


    public void setFolder(Path folder) {
        projectFolder = folder;
    }

    public Path getFolder() {
        return projectFolder;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public int getCardWidth() {
        return cardWidth;
    }

    public int getCardHeight() {
        return cardHeight;
    }
}
