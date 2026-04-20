package com.mycompany.cardcreator.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Predicate;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.CardElementType;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.util.ActionsManager;
import com.mycompany.cardcreator.util.AddedElementRecord;
import com.mycompany.cardcreator.util.ElementSnapshot;
import com.mycompany.cardcreator.util.SoundPlayer;

public class Toolbox extends JPanel {

    private Model model;
    private UUID cardID;
    private CardCanvas canvas;
    private ActionsManager actions;

    private JTextField textField;
    private boolean updatingTextField = false;

    // snapshot of the text element when the text field gained focus. committed
    // to the undo stack on focus loss so one editing session = one undo step
    private ElementSnapshot textBefore = null;
    private CardElement textTarget = null;

    // preset color palette shared by text and shape color sections
    private static final Color[] PALETTE = {
        Color.BLACK, Color.WHITE,
        Color.DARK_GRAY, Color.GRAY,
        Color.RED, new Color(200, 0, 0),
        Color.ORANGE, Color.YELLOW,
        Color.GREEN, new Color(0, 150, 0),
        Color.CYAN, new Color(0, 150, 150),
        Color.BLUE, new Color(0, 0, 180),
        Color.MAGENTA, new Color(150, 0, 150),
        new Color(139, 69, 19), new Color(210, 180, 140),
        new Color(255, 192, 203), new Color(255, 215, 0)
    };

    public Toolbox(Model model, UUID cardID, CardCanvas canvas, ActionsManager actions) {
        this.model = model;
        this.cardID = cardID;
        this.canvas = canvas;
        this.actions = actions;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Toolbox"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        setBackground(Color.WHITE);

        // TEXT
        JSpinner fontSpinner = buildTextSection();

        add(Box.createVerticalStrut(10));

        // TEXT COLOR
        buildTextColorSection();

        add(Box.createVerticalStrut(10));

        // SHAPES
        buildShapesSection();

        add(Box.createVerticalStrut(10));

        // SHAPE COLOR
        buildShapeColorSection();

        add(Box.createVerticalStrut(10));

        // ROTATION
        JSpinner rotationSpinner = buildRotationSection();

        add(Box.createVerticalStrut(10));

        // IMAGES
        buildImagesSection();

        add(Box.createVerticalStrut(10));

        // LAYERS
        JSpinner layerSpinner = buildLayerSection();

        add(Box.createVerticalGlue());

        // update toolbox values when a different element gets selected
        canvas.setOnSelectionChanged(() -> {
            // flush any pending text edit before switching target -- otherwise
            // the focus-lost listener fires after textTarget was reassigned
            commitTextEdit();
            CardElement sel = canvas.getSelectedElement();
            if (sel != null) {
                if (sel.type == CardElementType.TEXT) {
                    fontSpinner.setValue(sel.fontSize);
                    updatingTextField = true;
                    textField.setText(sel.text == null ? "" : sel.text);
                    updatingTextField = false;
                }
                rotationSpinner.setValue((int) sel.rotation);
                layerSpinner.setValue(sel.zLayer);
            }
        });
    }


    // records mutation as one coalesced change on the given element.
    // callers pass the selected element + a lambda that flips a field; we
    // snapshot before and after so the undo system can roll it back
    private void recordAndApply(CardElement el, Runnable mutation) {
        ElementSnapshot before = new ElementSnapshot(el);
        mutation.run();
        actions.recordChange(el, canvas, before, new ElementSnapshot(el));
    }

    // commits the pending text edit (if any). called on focus loss and when
    // the selection changes out from under the text field
    private void commitTextEdit() {
        if (textTarget != null && textBefore != null) {
            actions.recordChange(textTarget, canvas, textBefore, new ElementSnapshot(textTarget));
        }
        textTarget = null;
        textBefore = null;
    }


    // TEXT SECTION
    private JSpinner buildTextSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Text"));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        section.setBackground(Color.WHITE);

        JButton addTextBtn = new JButton("Add Text");
        addTextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addTextBtn.addActionListener(e -> {
            SoundPlayer.playClick();
            int offset = canvas.getElements().size() * 25;
            CardElement el = new CardElement(
                CardElementType.TEXT, 50 + offset, 50 + offset, 200, 50
            );
            model.addCardElement(cardID, el);
            canvas.addElement(el);
            actions.record(new AddedElementRecord(model, canvas, cardID, el));
        });
        section.add(addTextBtn);
        section.add(Box.createVerticalStrut(8));

        JLabel inputLabel = new JLabel("Text:");
        inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(inputLabel);

        textField = new JTextField();
        textField.setPreferredSize(new Dimension(140, 26));
        textField.setMaximumSize(new Dimension(140, 26));
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.addActionListener(e -> {
            canvas.clearSelection();
            canvas.requestFocusInWindow();
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if (updatingTextField) return;
                CardElement sel = canvas.getSelectedElement();
                if (sel != null && sel.type == CardElementType.TEXT) {
                    // capture: first keystroke in a focus session records
                    // the starting snapshot. commitTextEdit pushes it on focus
                    // loss (or when selection changes)
                    if (textTarget != sel) {
                        textBefore = new ElementSnapshot(sel);
                        textTarget = sel;
                    }
                    sel.text = textField.getText();
                    canvas.repaint();
                }
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitTextEdit();
            }
        });
        section.add(textField);

        section.add(Box.createVerticalStrut(8));

        JLabel fontLabel = new JLabel("Font Size:");
        fontLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(fontLabel);

        JSpinner fontSpinner = new JSpinner(new SpinnerNumberModel(24, 8, 200, 2));
        fontSpinner.setMaximumSize(new Dimension(100, 28));
        fontSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        fontSpinner.addChangeListener(e -> {
            CardElement sel = canvas.getSelectedElement();
            if (sel != null && sel.type == CardElementType.TEXT) {
                recordAndApply(sel, () -> {
                    sel.fontSize = (int) fontSpinner.getValue();
                    canvas.repaint();
                });
            }
        });
        section.add(fontSpinner);

        add(section);
        return fontSpinner;
    }


    // SHAPES SECTION
    private void buildShapesSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Shapes"));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        section.setBackground(Color.WHITE);

        addShapeButton(section, "Rectangle", CardElementType.RECTANGLE, 150, 100);
        section.add(Box.createVerticalStrut(4));
        addShapeButton(section, "Circle", CardElementType.CIRCLE, 150, 150);

        add(section);
    }

    // helper for adding shape buttons, avoids repeating code
    private void addShapeButton(JPanel section, String label, CardElementType type, int w, int h) {
        JButton btn = new JButton(label);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            SoundPlayer.playClick();
            int offset = canvas.getElements().size() * 25;
            CardElement el = new CardElement(type, 50 + offset, 50 + offset, w, h);
            model.addCardElement(cardID, el);
            canvas.addElement(el);
            actions.record(new AddedElementRecord(model, canvas, cardID, el));
        });
        section.add(btn);
    }


    // ROTATION SECTION
    private JSpinner buildRotationSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Rotation"));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        section.setBackground(Color.WHITE);

        JSpinner rotationSpinner = new JSpinner(new SpinnerNumberModel(0, -360, 360, 15));
        rotationSpinner.setMaximumSize(new Dimension(100, 28));
        rotationSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        rotationSpinner.addChangeListener(e -> {
            CardElement sel = canvas.getSelectedElement();
            if (sel != null) {
                recordAndApply(sel, () -> {
                    sel.rotation = (int) rotationSpinner.getValue();
                    canvas.repaint();
                });
            }
        });
        section.add(rotationSpinner);

        add(section);
        return rotationSpinner;
    }


    // SHAPE COLOR PALETTE
    private void buildShapeColorSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Shape Color"));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBackground(Color.WHITE);

        section.add(makePaletteGrid(el -> el.type == CardElementType.RECTANGLE || el.type == CardElementType.CIRCLE));
        add(section);
    }


    // TEXT COLOR PALETTE
    private void buildTextColorSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Text Color"));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBackground(Color.WHITE);

        section.add(makePaletteGrid(el -> el.type == CardElementType.TEXT));
        add(section);
    }


    // builds a swatch grid wired to apply color only when the predicate matches
    private JPanel makePaletteGrid(Predicate<CardElement> appliesTo) {
        JPanel colorGrid = new JPanel(new GridLayout(0, 4, 2, 2));
        colorGrid.setBackground(Color.WHITE);
        colorGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5 * 20));

        for (Color c : PALETTE) {
            JButton swatch = new JButton();
            swatch.setPreferredSize(new Dimension(18, 18));
            swatch.setMaximumSize(new Dimension(18, 18));
            swatch.setMinimumSize(new Dimension(18, 18));
            swatch.setMargin(new Insets(0, 0, 0, 0));
            swatch.setBackground(c);
            swatch.setOpaque(true);
            swatch.setBorderPainted(true);
            swatch.addActionListener(e -> {
                SoundPlayer.playClick();
                CardElement sel = canvas.getSelectedElement();
                if (sel != null && appliesTo.test(sel)) {
                    recordAndApply(sel, () -> {
                        sel.setColor(c);
                        canvas.repaint();
                    });
                }
            });
            colorGrid.add(swatch);
        }
        return colorGrid;
    }


    // IMAGES SECTION
    private void buildImagesSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Images"));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        section.setBackground(Color.WHITE);

        JButton importBtn = new JButton("Import Image");
        importBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        importBtn.addActionListener(e -> {
            SoundPlayer.playClick();
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "png", "jpg", "jpeg", "bmp", "gif"
            ));

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage img =
                        javax.imageio.ImageIO.read(chooser.getSelectedFile());
                    if (img != null) {
                        int offset = canvas.getElements().size() * 25;
                        CardElement el = new CardElement(
                            CardElementType.IMAGE,
                            50 + offset, 50 + offset,
                            img.getWidth(), img.getHeight()
                        );
                        // store path relative to the project folder so the
                        // project stays portable when moved to another machine
                        Path chosen = chooser.getSelectedFile().toPath();
                        Path projectFolder = model.getFolder();
                        if (projectFolder != null && chosen.isAbsolute()) {
                            el.imagePath = projectFolder.relativize(chosen).toString();
                        } else {
                            el.imagePath = chosen.toString();
                        }
                        el.zLayer = canvas.getElements().size();
                        model.addCardElement(cardID, el);
                        canvas.addElement(el);
                        actions.record(new AddedElementRecord(model, canvas, cardID, el));
                    }
                } catch (Exception ex) {
                    System.out.println("Error importing image: " + ex);
                }
            }
        });
        section.add(importBtn);
        add(section);
    }


    // LAYER SECTION
    private JSpinner buildLayerSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Layer"));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        section.setBackground(Color.WHITE);

        JSpinner layerSpinner = new JSpinner(new SpinnerNumberModel(0, -100, 100, 1));
        layerSpinner.setMaximumSize(new Dimension(100, 28));
        layerSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        layerSpinner.addChangeListener(e -> {
            CardElement sel = canvas.getSelectedElement();
            if (sel != null) {
                recordAndApply(sel, () -> {
                    sel.zLayer = (int) layerSpinner.getValue();
                    canvas.repaint();
                });
            }
        });
        section.add(layerSpinner);

        add(section);
        return layerSpinner;
    }
}
