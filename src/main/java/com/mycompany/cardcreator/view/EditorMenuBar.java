package com.mycompany.cardcreator.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;
import javax.swing.*;
import com.mycompany.cardcreator.model.FileIO;
import com.mycompany.cardcreator.model.Model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class EditorMenuBar extends JMenuBar {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JLabel lastSavedLabel = new JLabel("Last saved: --:--:--");


    public EditorMenuBar(Model model, UUID cardID, CardCanvas canvas, JFrame frame, Runnable onBack) {
        JMenu fileMenu = new JMenu("File");

        // back button - auto saves a preview then returns to card list
        JMenuItem backItem = new JMenuItem("Back");
        backItem.addActionListener(e -> {
            // save a preview so the card list thumbnail is always current
            try {
                BufferedImage preview = canvas.exportAsImage();
                File previewFile = new File(
                    model.getFolder(), "card_" + cardID + ".png"
                );
                javax.imageio.ImageIO.write(preview, "png", previewFile);
            } catch (Exception ex) {
                System.out.println("Could not save preview: " + ex);
            }

            frame.setJMenuBar(null);
            frame.revalidate();
            onBack.run();
        });

        // saves all element positions and model data to json
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            model.setImgX(canvas.getImgX());
            model.setImgY(canvas.getImgY());
            model.setImgW(canvas.getImgW());
            model.setImgH(canvas.getImgH());
            FileIO.saveModel(model);
            updateLastSaved();
            JOptionPane.showMessageDialog(frame, "Project saved.");
        });

        // exports the card as a png, also saves a preview for the card list
        JMenuItem exportItem = new JMenuItem("Export Card");
        exportItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(model.getFolder());
            chooser.setSelectedFile(new File(model.getFolder(), "card_export.png"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "PNG Image", "png"
            ));

            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
                {
                try {
                    BufferedImage exportImg = canvas.exportAsImage();
                    File outFile = chooser.getSelectedFile();
                    if (!outFile.getName().endsWith(".png")) {
                        outFile = new File(outFile.getAbsolutePath() + ".png");
                    }
                    javax.imageio.ImageIO.write(exportImg, "png", outFile);

                    // save preview copy for the card list thumbnails
                    File preview = new File(
                        model.getFolder(), "card_" + cardID + ".png"
                    );
                    javax.imageio.ImageIO.write(exportImg, "png", preview);
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
}
