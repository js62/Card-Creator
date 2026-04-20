package com.mycompany.cardcreator.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import javax.swing.*;
import com.mycompany.cardcreator.model.FileIO;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.util.SoundPlayer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class EditorMenuBar extends JMenuBar {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JLabel lastSavedLabel = new JLabel("Last saved: --:--:--");

    private final Model model;
    private final UUID cardID;
    private final CardCanvas canvas;


    public EditorMenuBar(Model model, UUID cardID, CardCanvas canvas, JFrame frame, Runnable onBack) {
        this.model = model;
        this.cardID = cardID;
        this.canvas = canvas;

        JMenu fileMenu = new JMenu("File");

        // back button - saves a preview then returns to card list
        JMenuItem backItem = new JMenuItem("Back");
        backItem.addActionListener(e -> {
            SoundPlayer.playClick();
            saveCardPreview();

            frame.setJMenuBar(null);
            frame.revalidate();
            onBack.run();
        });

        // saves all element positions and model data to json
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            SoundPlayer.playClick();
            model.setImgX(canvas.getImgX());
            model.setImgY(canvas.getImgY());
            model.setImgW(canvas.getImgW());
            model.setImgH(canvas.getImgH());
            FileIO.saveModel(model);
            saveCardPreview();
            updateLastSaved();
            JOptionPane.showMessageDialog(frame, "Project saved.");
        });

        // exports the card as a png, also saves a preview for the card list
        JMenuItem exportItem = new JMenuItem("Export Card");
        exportItem.addActionListener(e -> {
            SoundPlayer.playClick();
            // JFileChooser is a Swing component and works in File, so we
            // bridge to/from Path at this boundary
            JFileChooser chooser = new JFileChooser(model.getFolder().toFile());
            chooser.setSelectedFile(model.getFolder().resolve("card_export.png").toFile());
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "PNG Image", "png"
            ));

            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage exportImg = canvas.exportAsImage();
                    File outFile = chooser.getSelectedFile();
                    if (!outFile.getName().endsWith(".png")) {
                        outFile = new File(outFile.getAbsolutePath() + ".png");
                    }
                    javax.imageio.ImageIO.write(exportImg, "png", outFile);

                    // save preview copy for the card list thumbnails
                    Path preview = model.getFolder().resolve("card_" + cardID + ".png");
                    javax.imageio.ImageIO.write(exportImg, "png", preview.toFile());
                    JOptionPane.showMessageDialog(frame, "Exported to:\n" + outFile.getAbsolutePath());

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error exporting: " + ex.getMessage());
                }
            }
        });

        fileMenu.add(backItem);
        fileMenu.add(saveItem);
        fileMenu.add(exportItem);

        add(fileMenu);
        add(Box.createHorizontalStrut(10));
        add(lastSavedLabel);
    }


    public void updateLastSaved() {
        lastSavedLabel.setText("Last saved: " + LocalTime.now().format(TIME_FORMAT));
    }


    // re-renders the current card to a png so the card list thumbnail stays
    // fresh. called from manual save, back, and the autosave timer
    public void saveCardPreview() {
        try {
            BufferedImage preview = canvas.exportAsImage();
            Path previewFile = model.getFolder().resolve("card_" + cardID + ".png");
            javax.imageio.ImageIO.write(preview, "png", previewFile.toFile());
        } catch (Exception ex) {
            System.out.println("Could not save preview: " + ex);
        }
    }
}
