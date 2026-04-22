package com.mycompany.cardcreator.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Holds all the data for one project.
 *
 * A Model knows where the project folder is, how big the pages are, and
 * every card with the elements on it. Other parts of the app read and
 * change project data by calling methods here.
 */
public class Model {

    /**
     * Makes an empty model with default page size and no project folder.
     *
     * A Model in this state is ready to be filled in and then handed to
     * FileIO for a first save.
     */
    public Model() {
    }

    private Path projectFolder = null;

    //pdf output dimensions, divisble by grid size 25
    private int pageWidth = 675;
    private int pageHeight = 1075;

    //dimensions for the individual cards
    private int cardWidth = pageHeight / 4;
    private int cardHeight = pageWidth / 4;


    // all elements across all cards, keyed by uuid
    private LinkedHashMap<UUID, CardElement> cardElements = new LinkedHashMap<>();

    /**
     * Adds a new element to the given card.
     *
     * A new id is generated and put on the element. The element is then
     * added to the project and listed on the card. The returned id is the
     * same one stored on element.id.
     *
     * @param cardID  id of the card the element belongs to
     * @param element the element to add; its id field is overwritten
     * @return the id given to the element
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

    /**
     * Looks up an element by id.
     *
     * @param elementID the element id to look up
     * @return the matching CardElement, or null if no element has that id
     */
    public CardElement getCardElement(UUID elementID) {
        return cardElements.get(elementID);
    }

    /**
     * Removes the given element from the project and from its card.
     *
     * Does nothing if either id is unknown.
     *
     * @param cardID    id of the card the element is on
     * @param elementID id of the element to remove
     */
    public void removeCardElement(UUID cardID, UUID elementID) {
        cardElements.remove(elementID);

        Card card = cards.get(cardID);
        if (card != null) {
            card.removeElementID(elementID);
        }
    }

    /**
     * Puts a previously removed element back under its original id.
     *
     * Used by the undo system. Keeping the same id means any other record
     * still holding the element carries on pointing at the right object.
     * If the element is already present, or its id is null, the call does
     * nothing.
     *
     * @param cardID  id of the card to put the element back on
     * @param element the element to put back; its id must still be set
     */
    public void restoreCardElement(UUID cardID, CardElement element) {
        if (element.id == null) return;
        if (cardElements.containsKey(element.id)) return;

        cardElements.put(element.id, element);
        Card card = cards.get(cardID);
        if (card != null) {
            card.addElementID(element.id);
        }
    }

    /**
     * Returns the map of every element in the project, keyed by id.
     *
     * @return the live element map (not a copy)
     */
    public LinkedHashMap<UUID, CardElement> getCardElements() {
        return cardElements;
    }


    // all cards in the project
    private LinkedHashMap<UUID, Card> cards = new LinkedHashMap<>();

    /**
     * Looks up a card by id.
     *
     * @param CardID the card id to look up
     * @return the matching Card, or null if no card has that id
     */
    public Card getCard(UUID CardID) {
        return cards.get(CardID);
    }

    /**
     * Returns the ids of every card in the project, in the order they
     * were added.
     *
     * @return a snapshot list of the card ids
     */
    public ArrayList<UUID> getCardIDs() {
        return new ArrayList<>(cards.keySet());
    }

    /**
     * Adds a new empty card using the project's card dimensions.
     *
     * @return the id of the new card
     */
    public UUID addCard() {
        UUID id = UUID.randomUUID();
        Card c = new Card(cardWidth, cardHeight);
        cards.put(id, c);
        return id;
    }

    /**
     * Puts a card back under a known id with the given dimensions.
     *
     * Called by FileIO during load so that element ids stored against
     * that card stay consistent with what is on disk.
     *
     * @param id     the card id to reuse
     * @param width  card width in pixels
     * @param height card height in pixels
     */
    public void restoreCard(UUID id, int width, int height) {
        cards.put(id, new Card(width, height));
    }

    /**
     * Points the model at a project folder on disk.
     *
     * Every relative path on this model is resolved against this folder.
     *
     * @param folder the project root folder
     */
    public void setFolder(Path folder) {
        projectFolder = folder;
    }

    /**
     * Returns the project folder, or null if one has not been set.
     *
     * @return the project root folder
     */
    public Path getFolder() {
        return projectFolder;
    }

    /**
     * Returns the full page output width in pixels.
     *
     * @return pageWidth
     */
    public int getPageWidth() {
        return pageWidth;
    }

    /**
     * Returns the full page output height in pixels.
     *
     * @return pageHeight
     */
    public int getPageHeight() {
        return pageHeight;
    }

    /**
     * Returns the width of a single card in pixels.
     *
     * @return cardWidth
     */
    public int getCardWidth() {
        return cardWidth;
    }

    /**
     * Returns the height of a single card in pixels.
     *
     * @return cardHeight
     */
    public int getCardHeight() {
        return cardHeight;
    }
}
