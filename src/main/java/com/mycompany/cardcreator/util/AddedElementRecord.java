package com.mycompany.cardcreator.util;

import java.util.UUID;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.view.CardCanvas;

public class AddedElementRecord implements ElementRecord {

    private final Model model;
    private final CardCanvas canvas;
    private final UUID cardID;
    private final CardElement element;

    public AddedElementRecord(Model model, CardCanvas canvas, UUID cardID, CardElement element){
        this.model = model;
        this.canvas=canvas;
        this.cardID = cardID;
        this.element=element;
    }

    @Override
    public void undo() {
        canvas.removeElement(element);
        if (element.id != null) {
            model.removeCardElement(cardID, element.id);
        }
    }

    @Override
    public void redo() {
        // restoreCardElement reuses the existing uuid so other records that
        // still reference this element keep working
        model.restoreCardElement(cardID, element);
        canvas.addElement(element);
    }
}
