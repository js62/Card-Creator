package com.mycompany.cardcreator.model;

import java.util.UUID;

/**
 * Undo record for "user added this element".
 *
 * Undo removes the element from the canvas and the model. Redo puts it
 * back under the same id so any other record still holding the element
 * keeps working.
 */
public class AddedElementRecord implements ElementRecord {

    private final Model model;
    private final CanvasView canvas;
    private final UUID cardID;
    private final CardElement element;

    /**
     * Builds a record tracking that the given element was added.
     *
     * @param model   the Model the element belongs to
     * @param canvas  the canvas view the element was added to
     * @param cardID  id of the card the element is on
     * @param element the element that was just added
     */
    public AddedElementRecord(Model model, CanvasView canvas, UUID cardID, CardElement element){
        this.model = model;
        this.canvas=canvas;
        this.cardID = cardID;
        this.element=element;
    }

    /**
     * Removes the element from the canvas and the model.
     */
    @Override
    public void undo() {
        canvas.removeElement(element);
        if (element.id != null) {
            model.removeCardElement(cardID, element.id);
        }
    }

    /**
     * Puts the element back on the canvas and the model under its
     * original id.
     */
    @Override
    public void redo() {
        // restoreCardElement reuses the existing uuid so other records that
        // still reference this element keep working
        model.restoreCardElement(cardID, element);
        canvas.addElement(element);
    }
}
