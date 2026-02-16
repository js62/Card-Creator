package com.mycompany.cardcreator.Modules;


import javax.swing.JFileChooser;
import java.io.File;

public class ProjectOpener {

    public ProjectOpener() {
        
        
        JFileChooser chooser = new JFileChooser();

        // allow only folders to be selected
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            new Editor(selectedFolder);
        } else {
            // try again

        }

    }


}
