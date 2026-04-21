package com.mycompany.cardcreator.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;

/**
 * Right side panel of the editor that shows static help text.
 *
 * The text is hard coded because the instructions do not change between
 * sessions. If new features are added, the text here needs to be updated
 * too.
 */
public class InstructionsPanel extends JPanel {

    /**
     * Builds the panel and fills it with the how to use text.
     */
    public InstructionsPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(255, 0));
        setBorder(BorderFactory.createTitledBorder("How to use: "));
        setBackground(Color.WHITE);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.WHITE);
        textArea.setText(
        "1.) ADD CONTENT\n" +
        "    - Text : Click 'Add Text'\n" +
        "    - Shapes: Click 'Rectangle' or 'Circle'\n" +
        "    - Images: Click 'Import Image'\n\n" +

        "2.) SELECT & MOVEMENT\n" +
        "    - Select : Click on element\n" +
        "    - Drag : Move element\n" +
        "    - Rotate : Use rotation control\n" +
        "    - Layer : Use layer control\n" +
        "      *NOTE : Bigger number ->Front\n\n"+

        "3.) SHAPES\n" +
        "    - Resize : Drag corners\n" +
        "    - Outline : Click shape → Choose color\n" +
        "    - Fill : Right-click -> Fill -> Choose color\n\n" +

        "4.) TEXT\n" +
        "    - Color : Select text -> Choose color\n" +
        "    - Font size : Use font size control\n\n " +

        "5.) DELETE\n" +
        "    - Right-click element -> Delete\n\n" +

        "6.) SAVE\n" +
        "    - File -> Save project\n\n" +

        "7.) EXPORT\n" +
        "    - File -> Export Card\n\n" +
        "8.) UNDO / REDO\n" +
        "    - Edit -> Undo\n" +
        "    - Edit -> Redo\n" +
        "    *NOTE : Layer changes are\n" +
        "     not yet tracked by undo/redo"
        );

        JScrollPane scroll = new JScrollPane(textArea);
        add(scroll, BorderLayout.CENTER);
    }
}
