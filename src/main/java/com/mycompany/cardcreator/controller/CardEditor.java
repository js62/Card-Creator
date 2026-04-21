package com.mycompany.cardcreator.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.UUID;
import javax.swing.*;


import com.mycompany.cardcreator.model.Card;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.util.ActionsManager;
import com.mycompany.cardcreator.util.SoundPlayer;
import com.mycompany.cardcreator.view.CardCanvas;
import com.mycompany.cardcreator.view.EditorMenuBar;
import com.mycompany.cardcreator.view.InstructionsPanel;
import com.mycompany.cardcreator.view.Toolbox;

/**
 * Wires up one editing session for a single card.
 *
 * Builds the canvas, the toolbox, the instructions panel, and the menu
 * bar, then drops them into the given screen panel. Existing elements
 * for the card are loaded from the Model and a saved background image
 * is put back at its saved geometry.
 */
public class CardEditor {

    /**
     * Opens the editor for one card inside the given screen panel.
     *
     * @param model   the project Model being edited
     * @param cardID  id of the card to open
     * @param screen  the panel the editor widgets are added to
     * @param frame   the top level JFrame, used for the menu bar
     * @param onBack  callback fired when the user clicks Back on the menu
     */
    public CardEditor(Model model, UUID cardID, JPanel screen, JFrame frame, Runnable onBack) {
        SoundPlayer.playSound("Sounds/EditorLoadSound.wav");

        Card card = model.getCard(cardID);

        // session undo/redo. fresh instance each time the editor opens,
        // so starting a new editor never shows edits from a previous session
        ActionsManager actions = new ActionsManager();

        // CANVAS
        CardCanvas canvas = new CardCanvas(model, cardID, model.getPageWidth(), model.getPageHeight(), actions);
        screen.add(canvas, BorderLayout.CENTER);

        // LOAD EXISTING ELEMENTS FOR THIS CARD
        for (UUID elID : card.getElementIDs()) {
            CardElement el = model.getCardElement(elID);
            if (el != null) {
                canvas.addElement(el);
            }
        }

        // TOOLBOX
        Toolbox toolbox = new Toolbox(model, cardID, canvas, actions);
        JScrollPane toolboxScroll = new JScrollPane(toolbox);
        toolboxScroll.setBorder(null);
        toolboxScroll.getVerticalScrollBar().setUnitIncrement(16);
        toolboxScroll.setPreferredSize(new Dimension(180, 0));
        screen.add(toolboxScroll, BorderLayout.WEST);

        // INSTRUCTIONS PANEL
        screen.add(new InstructionsPanel(), BorderLayout.EAST);

        // RESTORE SAVED IMAGE
        restoreImage(model, canvas);

        // MENU BAR
        frame.setJMenuBar(new EditorMenuBar(model, cardID, canvas, frame, onBack, actions));
        frame.revalidate();
    }


    // RESTORE SAVED IMAGE
    private void restoreImage(Model model, CardCanvas canvas) {
        Path imagePath = model.resolveBackgroundImage();
        if (imagePath == null) {
            return;
        }
        try {
            BufferedImage img = javax.imageio.ImageIO.read(imagePath.toFile());

            if (img != null) {
                canvas.setBackgroundImage(img);
                if (model.getImgW() > 0 && model.getImgH() > 0) {
                    canvas.setImageBounds(
                        model.getImgX(), model.getImgY(),
                        model.getImgW(), model.getImgH()
                    );
                }
            }
        } catch (Exception ex) {
            System.out.println("Could not reload image: " + ex);
        }
    }
}
