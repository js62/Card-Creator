package com.mycompany.cardcreator.util;

import java.util.UUID;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.view.CardCanvas;

/**
 * Undo record for "user deleted this element".
 *
 * Undo puts the element back on the canvas and the model under the id it
 * had when it was deleted. Redo removes it again.
 */
public class DeletedElementRecord implements ElementRecord {

    private final Model model;
    private final CardCanvas canvas;
    private final UUID cardID;
    private final CardElement element;

    /**
     * Builds a record tracking that the given element was deleted.
     *
     * @param model   the Model the element came from
     * @param canvas  the CardCanvas the element was on
     * @param cardID  id of the card the element was on
     * @param element the element that was just deleted; its id must still be set
     */
    public DeletedElementRecord(Model model, CardCanvas canvas, UUID cardID, CardElement element){
        this.model=model;
        this.canvas = canvas;
        this.cardID=cardID;
        this.element = element;
    }

    /**
     * Puts the element back on the canvas and the model under its
     * original id.
     */
    @Override
    public void undo() {
        // put the element back with the same uuid it had before
        model.restoreCardElement(cardID, element);
        canvas.addElement(element);
    }

    /**
     * Removes the element from the canvas and the model.
     */
    @Override
    public void redo() {
        canvas.removeElement(element);
        if (element.id != null) {
            model.removeCardElement(cardID, element.id);
        }
    }
}
