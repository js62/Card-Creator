package com.mycompany.cardcreator;

import com.mycompany.cardcreator.view.ProjectOpener;
import javax.swing.SwingUtilities;

public class CardCreator {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new ProjectOpener());
    }
}