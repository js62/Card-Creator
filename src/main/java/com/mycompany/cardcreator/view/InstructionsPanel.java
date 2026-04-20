package com.mycompany.cardcreator.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;

public class InstructionsPanel extends JPanel {

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
        "8.) REDO - Ctrl+ Y\n" +
        "9.) UNDO - Ctrl+ Z"
        );

        JScrollPane scroll = new JScrollPane(textArea);
        add(scroll, BorderLayout.CENTER);
    }
}
