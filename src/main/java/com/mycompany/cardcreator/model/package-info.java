/**
 * Holds the data that describes a project and knows how to read/write it.
 *
 * The main types:
 *   Model           top-level container: project folder, page size,
 *                   background image, all the cards and elements
 *   Card            one page in the project; keeps a list of element ids
 *   CardElement     text, shape, or image sitting on a card
 *   CardElementType enum tag for what kind of element it is
 *   FileIO          save/load the Model to data.json in the project folder
 *
 * The view package reads these to draw the card, the controller package
 * changes them on user input, and the util package snapshots them for the
 * undo/redo.
 */
package com.mycompany.cardcreator.model;
