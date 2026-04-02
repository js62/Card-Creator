package com.mycompany.cardcreator.Modules;

import java.awt.*;
import java.io.File;
import java.util.UUID;
import javax.swing.*;

public class Editor {

    private Model model;
    private JFrame frame;
    private CardLayout layout;

    public Editor(File projectFolder) {

        model = FileIO.loadModel(projectFolder);
        if (model == null) {
            JOptionPane.showMessageDialog(null, "Failed to open project");
            return;
        }

        OpenWindow();
    }

    private void OpenWindow() {

        frame = new JFrame("Card Editor: " + model.getFolder().getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        layout = new CardLayout();
        frame.setLayout(layout);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel cardButtons = new JPanel();
        cardButtons.setLayout(new GridLayout(0, 3, 20, 20));

        addButtons(cardButtons);

        JScrollPane scrollPane = new JScrollPane(cardButtons);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(mainPanel, "MAIN");

        frame.setSize(400, 300);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private void addButtons(JPanel cardButtons) {

        int count = 1;
        for (UUID id : model.getCardIDs()) {

            JButton b = new JButton("Card #" + count);
            count++;

            b.setPreferredSize(new Dimension(200, 300));

            b.addActionListener((a) -> openEditor(id));

            cardButtons.add(b);
        }

        JButton newCardButton = new JButton("+ card");

        newCardButton.addActionListener((a) -> {
            model.addCard();
            cardButtons.removeAll();
            addButtons(cardButtons);
            cardButtons.revalidate();
            cardButtons.repaint();
        });

        cardButtons.add(newCardButton);
    }

    private void openEditor(UUID id) {

        JPanel editorPanel = new JPanel(new BorderLayout());

        new CardEditor(model, id, editorPanel);

        frame.add(editorPanel, id.toString());
        layout.show(frame.getContentPane(), id.toString());
    }
}