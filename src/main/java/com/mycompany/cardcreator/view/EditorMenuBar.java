package com.mycompany.cardcreator.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import javax.swing.*;
import com.mycompany.cardcreator.model.FileIO;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.util.ActionsManager;
import com.mycompany.cardcreator.util.SoundPlayer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


/**
 * Menu bar shown at the top of the editor window.
 *
 * Holds the File menu (Back, Save, Export) and the Edit menu (Undo,
 * Redo), plus a small "last saved" label on the right that gets updated
 * whenever the project is saved.
 */
public class EditorMenuBar extends JMenuBar {

    /** Time format used in the "last saved" label. */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Readout on the right of the bar showing the last save time. */
    private final JLabel lastSavedLabel = new JLabel("Last saved: --:--:--");

    private final Model model;
    private final UUID cardID;
    private final CardCanvas canvas;


    /**
     * Builds the menu bar and wires every menu item up to its action.
     *
     * @param model   the project Model being edited
     * @param cardID  id of the card currently open in the editor
     * @param canvas  the CardCanvas being edited, used for export and preview
     * @param frame   the top level JFrame, used for dialogs and file choosers
     * @param onBack  callback fired when the user clicks Back
     * @param actions the undo and redo stack for this editing session
     */
    public EditorMenuBar(Model model, UUID cardID, CardCanvas canvas, JFrame frame, Runnable onBack, ActionsManager actions) {
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

        // EDIT MENU -- undo / redo, menu-only. moving focus to the canvas
        // first forces any pending text edit to commit via focusLost so the
        // in-progress change lands on the stack before we pop it
        JMenu editMenu = new JMenu("Edit");

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(e -> {
            canvas.requestFocusInWindow();
            actions.undo();
            canvas.repaint();
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.addActionListener(e -> {
            canvas.requestFocusInWindow();
            actions.redo();
            canvas.repaint();
        });

        editMenu.add(undoItem);
        editMenu.add(redoItem);

        add(fileMenu);
        add(editMenu);
        add(Box.createHorizontalStrut(10));
        add(lastSavedLabel);
    }


    /**
     * Updates the "last saved" label to the current time.
     *
     * Called by the save menu item and by CardListView's autosave timer
     * so the user can see how fresh the save on disk is.
     */
    public void updateLastSaved() {
        lastSavedLabel.setText("Last saved: " + LocalTime.now().format(TIME_FORMAT));
    }


    /**
     * Writes a png thumbnail of the current card next to data.json.
     *
     * Called from the manual save, from Back, and from the autosave
     * timer, so the card list on the main window always has a fresh
     * thumbnail to show. Errors are printed to stdout and the call
     * returns without throwing.
     */
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
