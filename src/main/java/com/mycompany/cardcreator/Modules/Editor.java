package com.mycompany.cardcreator.Modules;

import com.mycompany.cardcreator.Modules.Model;
import javax.swing.JFrame;
import java.io.File;

public class Editor {

    public Editor(File projectFoler) {
        //create instance of model loaded from file path
        //run GUI
        OpenWindow();
    }
    
    
    
    private static void OpenWindow() {
        JFrame frame = new JFrame("Card editor");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(400, 300);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setVisible(true);
    }
}
