package com.mycompany.cardcreator.util;

import java.util.UUID;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.view.CardCanvas;

public class DeletedElementRecord implements ElementRecord {

    private final Model model;
    private final CardCanvas canvas;
    private final UUID cardID;
    private final CardElement element;

    public DeletedElementRecord(Model model, CardCanvas canvas, UUID cardID, CardElement element){
        this.model=model;
        this.canvas = canvas;
        this.cardID=cardID;
        this.element = element;
    }

    @Override
    public void undo() {
        // put the element back with the same uuid it had before
        model.restoreCardElement(cardID, element);
        canvas.addElement(element);
    }

    @Override
    public void redo() {
        canvas.removeElement(element);
        if (element.id != null) {
            model.removeCardElement(cardID, element.id);
        }
    }
}
