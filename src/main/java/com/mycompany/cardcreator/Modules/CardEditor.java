package com.mycompany.cardcreator.Modules;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CardEditor {

    private Card card;
    private List<UUID> elements;

    public CardEditor(Model model, UUID cardID, JPanel screen) {

        card = model.getCard(cardID);
        elements = card.getElementIDs();

        screen.setLayout(new BorderLayout());

        // Canvas
        CardCanvas canvas = new CardCanvas(card.getWidth(), card.getHeight());
        screen.add(canvas, BorderLayout.CENTER);

        // LEFT panel (vertical buttons)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        // =========================
        // BACK BUTTON (FIXED)
        // =========================
        JButton back = new JButton("← Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.setMaximumSize(new Dimension(150, 30));

        back.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    screen,
                    "Are you sure you want to leave the card editor?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(screen);

                if (frame != null) {
                    CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
                    layout.show(frame.getContentPane(), "MAIN");
                }
            }
            // NO → do nothing
        });

        // =========================
        // IMPORT BUTTON
        // =========================
        JButton importBtn = new JButton("Import Image");
        importBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        importBtn.setMaximumSize(new Dimension(150, 30));

        importBtn.addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Images", "png", "jpg", "jpeg", "bmp", "gif"));

            int result = chooser.showOpenDialog(screen);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        canvas.setBackgroundImage(img);

                        // Save state
                        model.setBackgroundImagePath(file.getAbsolutePath());
                        model.setImgX(canvas.getImgX());
                        model.setImgY(canvas.getImgY());
                        model.setImgW(canvas.getImgW());
                        model.setImgH(canvas.getImgH());
                        FileIO.saveModel(model);

                        JOptionPane.showMessageDialog(screen, "Image imported and project saved.");
                    } else {
                        JOptionPane.showMessageDialog(screen, "Could not read image file.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(screen, "Error loading image: " + ex.getMessage());
                }
            }
        });

        // =========================
        // EXPORT BUTTON
        // =========================
        JButton exportBtn = new JButton("Export Image");
        exportBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportBtn.setMaximumSize(new Dimension(150, 30));

        exportBtn.addActionListener(e -> {
            try {
                BufferedImage exportImg = canvas.exportAsImage();

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save Card Image");
                chooser.setSelectedFile(new File("card.png"));

                int result = chooser.showSaveDialog(screen);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();

                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }

                    ImageIO.write(exportImg, "png", file);
                    JOptionPane.showMessageDialog(screen, "Image exported successfully!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(screen, "Error exporting image: " + ex.getMessage());
            }
        });

        // =========================
        // ADD COMPONENTS
        // =========================
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(back);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(importBtn);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(exportBtn);
        leftPanel.add(Box.createVerticalGlue());

        screen.add(leftPanel, BorderLayout.WEST);

        // Bottom label
        JLabel label = new JLabel("Editing Card: " + cardID);
        screen.add(label, BorderLayout.SOUTH);

        // Restore saved image
        if (model.getBackgroundImagePath() != null) {
            try {
                BufferedImage img = ImageIO.read(new File(model.getBackgroundImagePath()));
                if (img != null) {
                    canvas.setBackgroundImage(img);
                    if (model.getImgW() > 0 && model.getImgH() > 0) {
                        canvas.setImageBounds(
                                model.getImgX(),
                                model.getImgY(),
                                model.getImgW(),
                                model.getImgH()
                        );
                    }
                }
            } catch (Exception ex) {
                System.out.println("Could not reload image: " + ex);
            }
        }
    }
}