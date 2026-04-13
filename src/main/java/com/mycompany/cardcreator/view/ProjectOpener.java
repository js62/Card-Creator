package com.mycompany.cardcreator.view;

import java.awt.GridLayout;
import javax.swing.*;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import com.mycompany.cardcreator.model.FileIO;
import com.mycompany.cardcreator.controller.Editor;

public class ProjectOpener {

    public ProjectOpener() {

        JFrame frame = new JFrame("Open Project");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 20, 20));

        JButton createFolder = new JButton("New Project");
        JButton selectFolder = new JButton("Select Project");

        createFolder.addActionListener((e) -> {
            if (ChooseProject(true)) frame.dispose();
        });

        selectFolder.addActionListener((e) -> {
            if (ChooseProject(false)) frame.dispose();
        });

        panel.add(createFolder);
        panel.add(selectFolder);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.add(panel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setVisible(true);
    }

    private boolean ChooseProject(boolean create) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            if (create) {
                if (selectedFolder.isDirectory() && selectedFolder.list().length == 0) {
                    FileIO.createProject(selectedFolder);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select empty folder for your new.");
                    return false;
                }
            }
            new Editor(selectedFolder);
            return true;
        }
        return false;
    }
}