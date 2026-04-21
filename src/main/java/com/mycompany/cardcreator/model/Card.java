package com.mycompany.cardcreator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One page in a project.
 *
 * A card knows its own width and height and keeps an ordered list of the
 * element ids sitting on it. The actual CardElement objects are owned by
 * the Model, so a card only carries ids.
 */
public class Card {

    /** ids of the elements placed on this card, in the order they were added. */
    private List<UUID> elements;

    /** width of this card in pixels. */
    private final int width;

    /** height of this card in pixels. */
    private final int height;

    /**
     * Creates a new empty card with the given page size.
     *
     * @param width  card width in pixels, must be positive
     * @param height card height in pixels, must be positive
     */
    public Card(int width, int height){
        this.width=width;
        this.height=height;
        this.elements=new ArrayList<>();
    }

    /**
     * Returns the live list of element ids on this card.
     *
     * Callers that mutate the list change the card directly, so the Model
     * uses this to add and remove ids during edits.
     *
     * @return the backing list of element ids (not a copy)
     */
    public List<UUID> getElementIDs(){
        return elements;
    }

    /**
     * Appends an element id to the end of this card.
     *
     * No check is made for duplicates; the caller is responsible for only
     * adding ids that belong to this card.
     *
     * @param id the element id to add
     */
    public void addElementID(UUID id){
        elements.add(id);
    }

    /**
     * Drops the given element id from this card.
     *
     * Does nothing if the id isn't on this card.
     *
     * @param id the element id to remove
     */
    public void removeElementID(UUID id){
        elements.remove(id);
    }

    /**
     * Returns the width of this card in pixels.
     *
     * @return the card's width
     */
    public int getWidth(){
        return width;
    }

    /**
     * Returns the height of this card in pixels.
     *
     * @return the card's height
     */
    public int getHeight(){
        return height;
    }
}
