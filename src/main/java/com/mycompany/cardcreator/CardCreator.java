package com.mycompany.cardcreator;

import com.mycompany.cardcreator.view.ProjectOpener;
import javax.swing.SwingUtilities;

/**
 * Application entry point.
 *
 * Launches the first window (ProjectOpener) on the Swing event thread.
 * Nothing else happens here; every screen after this point is built
 * from the controller package.
 */
public class CardCreator {

    /**
     * Starts the app.
     *
     * @param args command line arguments, not used
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new ProjectOpener());
    }
}