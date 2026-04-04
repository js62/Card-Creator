package com.mycompany.cardcreator.controller;

import java.awt.BorderLayout;
import java.util.UUID;
import javax.swing.*;
import com.mycompany.cardcreator.model.Card;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.CardElementType;
import com.mycompany.cardcreator.model.FileIO;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.view.CardCanvas;

/**
 * Controller for editing a single card. Sets up the canvas,
 * toolbox, and menu bar for this card.
 */
public class CardEditor {

    private Card card;
    private UUID cardID;
    private CardCanvas canvas;

    public CardEditor(Model model, UUID cardID, JPanel screen, JFrame frame, Runnable onBack) {
        this.cardID = cardID;
        card = model.getCard(cardID);

        // CANVAS
        canvas = new CardCanvas(model.getPageWidth(), model.getPageHeight());
        screen.add(canvas, BorderLayout.CENTER);

        // LOAD EXISTING ELEMENTS FOR THIS CARD
        for (UUID elID : card.getElementIDs()) {
            CardElement el = model.getCardElement(elID);
            if (el != null) {
                canvas.addElement(el);
            }
        }

        // TOOLBOX
        JPanel leftPane = buildToolbox(model, canvas);
        screen.add(leftPane, BorderLayout.WEST);

        // RESTORE SAVED IMAGE
        restoreImage(model);

        // MENU BAR
        buildMenuBar(model, frame, onBack);
    }


    // TOOLBOX
    private JPanel buildToolbox(Model model, CardCanvas canvas) {
        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Toolbox"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        leftPane.setPreferredSize(new java.awt.Dimension(160, 0));
        leftPane.setBackground(java.awt.Color.WHITE);

        // TEXT
        JSpinner fontSpinner = buildTextSection(leftPane, model);

        leftPane.add(Box.createVerticalStrut(10));

        // SHAPES
        buildShapesSection(leftPane, model);

        leftPane.add(Box.createVerticalStrut(10));

        // ROTATION
        JSpinner rotationSpinner = buildRotationSection(leftPane);

        leftPane.add(Box.createVerticalStrut(10));

        // COLORS
        buildColorSection(leftPane);

        leftPane.add(Box.createVerticalStrut(10));

        // IMAGES
        buildImagesSection(leftPane, model);

        leftPane.add(Box.createVerticalStrut(10));

        // LAYERS
        JSpinner layerSpinner = buildLayerSection(leftPane);

        leftPane.add(Box.createVerticalGlue());

        // update toolbox values when a different element gets selected
        canvas.setOnSelectionChanged(() -> {
            CardElement sel = canvas.getSelectedElement();
            if (sel != null) {
                if (sel.type == CardElementType.TEXT) {
                    fontSpinner.setValue(sel.fontSize);
                }
                rotationSpinner.setValue((int) sel.rotation);
                layerSpinner.setValue(sel.zLayer);
            }
        });

        return leftPane;
    }


    // TEXT SECTION
    private JSpinner buildTextSection(JPanel leftPane, Model model) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Text"));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 120));
        section.setBackground(java.awt.Color.WHITE);

        JButton addTextBtn = new JButton("Add Text");
        addTextBtn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        addTextBtn.addActionListener(e -> {
            int offset = canvas.getElements().size() * 25;
            CardElement el = new CardElement(
                CardElementType.TEXT, 50 + offset, 50 + offset, 200, 50
            );
            model.addCardElement(cardID, el);
            canvas.addElement(el);
        });
        section.add(addTextBtn);
        section.add(Box.createVerticalStrut(8));

        JLabel fontLabel = new JLabel("Font Size:");
        fontLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        section.add(fontLabel);

        JSpinner fontSpinner = new JSpinner(new SpinnerNumberModel(24, 8, 200, 2));
        fontSpinner.setMaximumSize(new java.awt.Dimension(100, 28));
        fontSpinner.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        fontSpinner.addChangeListener(e -> {
            CardElement sel = canvas.getSelectedElement();
            if (sel != null && sel.type == CardElementType.TEXT) {
                sel.fontSize = (int) fontSpinner.getValue();
                canvas.repaint();
            }
        });
        section.add(fontSpinner);

        leftPane.add(section);
        return fontSpinner;
    }


    // SHAPES SECTION
    private void buildShapesSection(JPanel leftPane, Model model) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Shapes"));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 130));
        section.setBackground(java.awt.Color.WHITE);

        addShapeButton(section, "Rectangle", CardElementType.RECTANGLE, 150, 100, model);
        section.add(Box.createVerticalStrut(4));
        addShapeButton(section, "Circle", CardElementType.CIRCLE, 150, 150, model);

        leftPane.add(section);
    }

    // helper for adding shape buttons, avoids repeating code
    private void addShapeButton(JPanel section, String label, CardElementType type, int w, int h, Model model) {
        JButton btn = new JButton(label);
        btn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            int offset = canvas.getElements().size() * 25;
            CardElement el = new CardElement(type, 50 + offset, 50 + offset, w, h);
            model.addCardElement(cardID, el);
            canvas.addElement(el);
        });
        section.add(btn);
    }


    // ROTATION SECTION
    private JSpinner buildRotationSection(JPanel leftPane) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Rotation"));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
        section.setBackground(java.awt.Color.WHITE);

        JSpinner rotationSpinner = new JSpinner(new SpinnerNumberModel(0, -360, 360, 15));
        rotationSpinner.setMaximumSize(new java.awt.Dimension(100, 28));
        rotationSpinner.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        rotationSpinner.addChangeListener(e -> {
            CardElement sel = canvas.getSelectedElement();
            if (sel != null) {
                sel.rotation = (int) rotationSpinner.getValue();
                canvas.repaint();
            }
        });
        section.add(rotationSpinner);

        leftPane.add(section);
        return rotationSpinner;
    }


    // COLOR PALETTE
    private void buildColorSection(JPanel leftPane) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Color"));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.setBackground(java.awt.Color.WHITE);

        // preset color palette
        java.awt.Color[] palette = {
            java.awt.Color.BLACK, java.awt.Color.WHITE,
            java.awt.Color.DARK_GRAY, java.awt.Color.GRAY,
            java.awt.Color.RED, new java.awt.Color(200, 0, 0),
            java.awt.Color.ORANGE, java.awt.Color.YELLOW,
            java.awt.Color.GREEN, new java.awt.Color(0, 150, 0),
            java.awt.Color.CYAN, new java.awt.Color(0, 150, 150),
            java.awt.Color.BLUE, new java.awt.Color(0, 0, 180),
            java.awt.Color.MAGENTA, new java.awt.Color(150, 0, 150),
            new java.awt.Color(139, 69, 19), new java.awt.Color(210, 180, 140),
            new java.awt.Color(255, 192, 203), new java.awt.Color(255, 215, 0)
        };

        JPanel colorGrid = new JPanel(new java.awt.GridLayout(0, 4, 2, 2));
        colorGrid.setBackground(java.awt.Color.WHITE);
        colorGrid.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 5 * 20));

        for (java.awt.Color c : palette) {
            JButton swatch = new JButton();
            swatch.setPreferredSize(new java.awt.Dimension(18, 18));
            swatch.setMaximumSize(new java.awt.Dimension(18, 18));
            swatch.setMinimumSize(new java.awt.Dimension(18, 18));
            swatch.setMargin(new java.awt.Insets(0, 0, 0, 0));
            swatch.setBackground(c);
            swatch.setOpaque(true);
            swatch.setBorderPainted(true);
            swatch.addActionListener(e -> {
                CardElement sel = canvas.getSelectedElement();
                if (sel != null) {
                    sel.setColor(c);
                    canvas.repaint();
                }
            });
            colorGrid.add(swatch);
        }
        section.add(colorGrid);
        leftPane.add(section);
    }


    // IMAGES SECTION
    private void buildImagesSection(JPanel leftPane, Model model) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Images"));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
        section.setBackground(java.awt.Color.WHITE);

        JButton importBtn = new JButton("Import Image");
        importBtn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "png", "jpg", "jpeg", "bmp", "gif"
            ));

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.awt.image.BufferedImage img =
                        javax.imageio.ImageIO.read(chooser.getSelectedFile());
                    if (img != null) {
                        int offset = canvas.getElements().size() * 25;
                        CardElement el = new CardElement(
                            CardElementType.IMAGE,
                            50 + offset, 50 + offset,
                            img.getWidth(), img.getHeight()
                        );
                        el.imagePath = chooser.getSelectedFile().getAbsolutePath();
                        el.zLayer = canvas.getElements().size();
                        model.addCardElement(cardID, el);
                        canvas.addElement(el);
                    }
                } catch (Exception ex) {
                    System.out.println("Error importing image: " + ex);
                }
            }
        });
        section.add(importBtn);
        leftPane.add(section);
    }


    // LAYER SECTION
    private JSpinner buildLayerSection(JPanel leftPane) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Layer"));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 60));
        section.setBackground(java.awt.Color.WHITE);

        JSpinner layerSpinner = new JSpinner(new SpinnerNumberModel(0, -100, 100, 1));
        layerSpinner.setMaximumSize(new java.awt.Dimension(100, 28));
        layerSpinner.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        layerSpinner.addChangeListener(e -> {
            CardElement sel = canvas.getSelectedElement();
            if (sel != null) {
                sel.zLayer = (int) layerSpinner.getValue();
                canvas.repaint();
            }
        });
        section.add(layerSpinner);

        leftPane.add(section);
        return layerSpinner;
    }


    // RESTORE SAVED IMAGE
    private void restoreImage(Model model) {
        if (model.getBackgroundImagePath() == null) {
            return;
        }
        try {
            java.awt.image.BufferedImage img =
                javax.imageio.ImageIO.read(new java.io.File(model.getBackgroundImagePath()));

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


    // MENU BAR
    private void buildMenuBar(Model model, JFrame frame, Runnable onBack) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        // back button - auto saves a preview then returns to card list
        JMenuItem backItem = new JMenuItem("Back");
        backItem.addActionListener(e -> {
            // save a preview so the card list thumbnail is always current
            try {
                java.awt.image.BufferedImage preview = canvas.exportAsImage();
                java.io.File previewFile = new java.io.File(
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
            JOptionPane.showMessageDialog(frame, "Project saved.");
        });

        // exports the card as a png, also saves a preview for the card list
        JMenuItem exportItem = new JMenuItem("Export Card");
        exportItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(model.getFolder());
            chooser.setSelectedFile(new java.io.File(model.getFolder(), "card_export.png"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "PNG Image", "png"
            ));

            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.awt.image.BufferedImage exportImg = canvas.exportAsImage();
                    java.io.File outFile = chooser.getSelectedFile();
                    if (!outFile.getName().endsWith(".png")) {
                        outFile = new java.io.File(outFile.getAbsolutePath() + ".png");
                    }
                    javax.imageio.ImageIO.write(exportImg, "png", outFile);

                    // save preview copy for the card list thumbnails
                    java.io.File preview = new java.io.File(
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
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
        frame.revalidate();
    }
}
