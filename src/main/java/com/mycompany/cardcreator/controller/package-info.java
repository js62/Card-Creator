/**
 * Ties the view screens to the model and handles user input.
 *
 * What's here:
 *   CardListView          the project's main window; shows a thumbnail grid
 *                         of the cards and opens the editor when one is clicked
 *   CardEditor            wires up one editing session: builds the canvas,
 *                         toolbox, menu bar, and instructions for a single card
 *   CanvasMouseController mouse listener for CardCanvas; does selection,
 *                         drag, resize, right-click menus, and pushes each
 *                         gesture onto the undo stack as one step
 *
 * The controller reads and mutates the model package in response to clicks
 * and drags, then asks the view to repaint.
 */
package com.mycompany.cardcreator.controller;
