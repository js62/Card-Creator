package com.mycompany.cardcreator.Modules;

//import javax.swing.JFrame;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.*;
import java.io.File;
import javax.swing.JOptionPane;

/**
 * Main editor window for a card project. Provides import, save, and export
 * functionality.
 */
public class Editor {

    private Model model;
    private JFrame frame;
    private CardCanvas canvas;

    public Editor(File projectFolder) {
        //create instance of model loaded from file path
        model = FileIO.loadModel(projectFolder);
        if (model == null) {
            JOptionPane.showMessageDialog(null, "Failed to open project");
            return;
        }
        //run GUI
        OpenWindow();
        //
    }

    private void OpenWindow() {

        /* TODO: this window should use what swing calls a "card layout." This 
        will allow one screen to be pushed on top of another and popped off 
        later. The main screen should display all cards (which are clickable).
        When one is clicked, a new screen (Jpanel) should be pushed on top of
        the main screen. This panel will contain the card canvas and edit 
        options for this card.
        
        The Jpanel that is pushed should be coded in the CardEditor file.
         */
        frame = new JFrame("Card Editor: " + model.getFolder().getName());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new CardLayout());

        JPanel cardButtons = new JPanel();

        cardButtons.setLayout(new GridLayout(0, 3, 20, 20));

        // +20 is just to see layout while testing when there aren't any cards yet
        for (int i = 0; i < model.getCardIDs().size() + 20; i++) {
            JButton b = new JButton("#" + i);
            b.setPreferredSize(new Dimension(100, 180));
            cardButtons.add(b);
        }
        JButton newCardButton = new JButton("+ card");

        newCardButton.addActionListener((a) -> {

        });

        cardButtons.add(newCardButton);

        JScrollPane scrollPane = new JScrollPane(cardButtons);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane);

        frame.setSize(400, 300);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setVisible(true);
    }
}
