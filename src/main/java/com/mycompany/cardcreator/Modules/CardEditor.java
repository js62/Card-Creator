
package com.mycompany.cardcreator.Modules;

import java.util.UUID;
import javax.swing.JPanel;
import java.util.List;


public class CardEditor {
    
    private Card card;
    private List<UUID> elements;

    public CardEditor(Model model, UUID cardID, JPanel screen) {
        card=model.getCard(cardID);
        elements=card.getElementIDs();
    }
    
}
