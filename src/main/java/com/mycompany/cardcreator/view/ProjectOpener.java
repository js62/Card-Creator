package com.mycompany.cardcreator.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import com.mycompany.cardcreator.model.FileIO;
import com.mycompany.cardcreator.controller.CardListView;
import com.mycompany.cardcreator.util.SoundPlayer;

/**
 * First window shown when the app starts.
 *
 * Gives the user two choices: create a brand new project in an empty
 * folder, or open an existing project folder. When the user picks one,
 * this window closes and CardListView takes over.
 */
public class ProjectOpener {

    /**
     * Builds and shows the open-project window.
     */
    public ProjectOpener() {

        JFrame frame = new JFrame("Open Project");
        JPanel panel = new JPanel(new BorderLayout(20, 20));

        JButton createFolder = new JButton("New Project");
        JButton selectFolder = new JButton("Select Project");

        // LEFT SIDE (buttons stacked)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 20, 20));

        createFolder.addActionListener((e) -> {
            SoundPlayer.playClick();
            if (ChooseProject(true)) frame.dispose();
        });

        selectFolder.addActionListener((e) -> {
            SoundPlayer.playClick();
            if (ChooseProject(false)) frame.dispose();
        });

        buttonPanel.add(createFolder);
        buttonPanel.add(selectFolder);

        // RIGHT SIDE (instructions)
        JTextArea instructions = new JTextArea(
            "Instructions:\n\n" +
            "1.) NEW PROJECT\n" +
            "    - Create an EMPTY folder\n" +
            "    *NOTE: If error occurs, delete saved folder.\n"+
            "2.) SAVED PROJECT\n" +
            "    - Choose an existing saved project folder.\n\n"  
        );

        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(instructions);

        // ADD TO MAIN PANEL
        panel.add(buttonPanel, BorderLayout.WEST);   // left
        panel.add(scrollPane, BorderLayout.CENTER);  // right

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.add(panel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 250); // slightly wider for right-side instructions
        frame.setVisible(true);
    }

    private boolean ChooseProject(boolean create) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            // bridge from Swing's File API to nio Path 
            File selectedFile = chooser.getSelectedFile();
            Path selectedFolder = selectedFile.toPath();

            if (create) {
                boolean isEmptyDir = Files.isDirectory(selectedFolder)
                    && selectedFile.list() != null
                    && selectedFile.list().length == 0;

                if (isEmptyDir) {
                    FileIO.createProject(selectedFolder);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select empty folder for your new.");
                    return false;
                }
            }
            new CardListView(selectedFolder);
            return true;
        }
        return false;
    }
}