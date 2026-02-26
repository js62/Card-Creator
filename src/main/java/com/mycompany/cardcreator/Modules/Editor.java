package com.mycompany.cardcreator.Modules;

import com.mycompany.cardcreator.Modules.Model;
//import javax.swing.JFrame;
import javax.swing.*;
import java.io.File;
import javax.swing.JOptionPane;

/**
 * Main editor window for a card project.
 * Provides import, save, and export functionality.
 */
public class Editor {
    private Model model;
    private JFrame frame;
    private CardCanvas canvas;

    public Editor(File projectFolder) {
        //create instance of model loaded from file path
        //run GUI
        model = FileIO.loadModel(projectFolder);
        if (model == null){
            JOptionPane.showMessageDialog(null, "Failed to open project");
            return;
        }
        OpenWindow();
    }
    
    
    
    private  void OpenWindow() {
        frame = new JFrame("Card Editor: " + model.getFolder().getName());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        // Import — opens file chooser to load an image onto the canvas
        JMenuItem importItem = new JMenuItem("Import Image");
        importItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "png", "jpg", "jpeg", "bmp", "gif"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(chooser.getSelectedFile());
                    if (img != null) {
                        canvas.setBackgroundImage(img);
                        model.setBackgroundImagePath(chooser.getSelectedFile().getAbsolutePath());
                        FileIO.saveModel(model);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Could not read image file.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error loading image: " + ex.getMessage());
                }
            }
        });
        
        
        
        
       
        // Save — writes image position/size and all model data to data.json
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            model.setImgX(canvas.getImgX());
            model.setImgY(canvas.getImgY());
            model.setImgW(canvas.getImgW());
            model.setImgH(canvas.getImgH());
            FileIO.saveModel(model);
            JOptionPane.showMessageDialog(frame, "Project saved.");
        });

        // Export — saves the card as a PNG image file
        JMenuItem exportItem = new JMenuItem("Export Card");
        exportItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(model.getFolder());
            chooser.setSelectedFile(new java.io.File(model.getFolder(), "card_export.png"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "PNG Image", "png"));
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.awt.image.BufferedImage exportImg = canvas.exportAsImage();
                    java.io.File outFile = chooser.getSelectedFile();
                    if (!outFile.getName().endsWith(".png")) {
                        outFile = new java.io.File(outFile.getAbsolutePath() + ".png");
                    }
                    javax.imageio.ImageIO.write(exportImg, "png", outFile);
                    JOptionPane.showMessageDialog(frame, "Exported to:\n" + outFile.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error exporting: " + ex.getMessage());
                }
            }
        });
        
        fileMenu.add(saveItem);
        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
        
        canvas = new CardCanvas(model.getCanvasWidth(), model.getCanvasHeight());
        frame.add(canvas, java.awt.BorderLayout.CENTER);

        // Restore saved image and its position/size if available
        if (model.getBackgroundImagePath() != null) {
            try {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(
                        new java.io.File(model.getBackgroundImagePath()));
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

        frame.setSize(400, 300);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setVisible(true);
    }
}
