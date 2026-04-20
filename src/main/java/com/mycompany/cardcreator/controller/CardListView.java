package com.mycompany.cardcreator.controller;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.model.FileIO;
import com.mycompany.cardcreator.view.EditorMenuBar;

/**
 * Main editor window. Shows all cards in a project and lets
 * you click into each one to edit it.
 */
public class CardListView {

    private Model model;
    private JFrame frame;

    public CardListView(Path projectFolder) {
        //load project from file
        model = FileIO.loadModel(projectFolder);
        if (model == null) {
            JOptionPane.showMessageDialog(null, "Failed to open project");
            return;
        }
        OpenWindow();
    }
    
    
    private void autoSave() {
        FileIO.saveModel(model);

        // if the editor view is open, also refresh the thumbnail and the
        // last saved indicator on the menu bar
        if (frame != null && frame.getJMenuBar() instanceof EditorMenuBar) {
            EditorMenuBar bar = (EditorMenuBar) frame.getJMenuBar();
            bar.saveCardPreview();
            bar.updateLastSaved();
        }
    }

    private void OpenWindow() {

        frame = new JFrame("Card Editor: " + model.getFolder().getFileName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel(new CardLayout());
        frame.add(container);

        JPanel cardButtons = new JPanel();
        cardButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

        addButtons(cardButtons, container);

        JScrollPane scrollPane = new JScrollPane(cardButtons);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        container.add(scrollPane, "main");

        frame.setSize(400, 300);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        
        // auto save every 10 seconds
        Timer autoSaveTimer=new Timer(10000, e -> autoSave());
        autoSaveTimer.start();
    }

    private void addButtons(JPanel cardButtons, JPanel container) {
        CardLayout cl = (CardLayout) container.getLayout();

        // button size matches card aspect ratio
        int btnW = 150;
        int btnH = (int)(btnW * ((double) model.getPageHeight() / model.getPageWidth()));
        Dimension btnSize = new Dimension(btnW, btnH);

        int cardNumber = 1;
        for (UUID id : model.getCardIDs()) {

            JButton b = new JButton("Card " + cardNumber);
            b.setPreferredSize(btnSize);

            // check if theres a preview image from a previous export
            Path preview = model.getFolder().resolve("card_" + id + ".png");
            if (Files.exists(preview)) {
                try {
                    Image img = javax.imageio.ImageIO.read(preview.toFile());
                    if (img != null) {
                        img = img.getScaledInstance(btnW, btnH, Image.SCALE_SMOOTH);
                        b.setIcon(new ImageIcon(img));
                        b.setText("");
                        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
                        b.setContentAreaFilled(false);
                        b.setBorderPainted(true);
                    }
                } catch (Exception ex) {
                    //just keep default text if loading fails
                }
            }

            cardButtons.add(b);

            b.addActionListener((a) -> {
                String panelName = "card-" + id.toString();
                JPanel editorPanel = new JPanel(new java.awt.BorderLayout());
                new CardEditor(model, id, editorPanel, frame, () -> {
                    // rebuild buttons when coming back so previews update
                    cardButtons.removeAll();
                    addButtons(cardButtons, container);
                    cardButtons.revalidate();
                    cardButtons.repaint();
                    cl.show(container, "main");
                    FileIO.saveModel(model);
                });
                container.add(editorPanel, panelName);
                cl.show(container, panelName);
            });

            cardNumber++;
        }

        JButton newCardButton = new JButton("+ card");
        newCardButton.setPreferredSize(btnSize);

        newCardButton.addActionListener((a) -> {
            model.addCard();
            FileIO.saveModel(model);
            cardButtons.removeAll();
            addButtons(cardButtons, container);
            cardButtons.revalidate();
            cardButtons.repaint();
        });

        cardButtons.add(newCardButton);
    }
}
