/**
 * Holds the data that describes a project, knows how to read/write it,
 * and keeps the undo/redo history of edits to that data.
 *
 * Project data:
 *   Model           top-level container: project folder, page size, and
 *                   all the cards and elements
 *   Card            one page in the project; keeps a list of element ids
 *   CardElement     text, shape, or image sitting on a card
 *   CardElementType enum tag for what kind of element it is
 *   FileIO          save/load the Model to data.json in the project folder
 *
 * Undo/redo:
 *   ActionsManager         keeps the undo and redo stacks, merges rapid
 *                          changes on the same element into one step
 *   ElementRecord          interface every undoable action implements
 *   AddedElementRecord     element was spawned; undo removes it, redo puts it back
 *   DeletedElementRecord   element was deleted; undo puts it back, redo removes it
 *   ChangedElementRecord   element was changed (moved, resized, recolored, etc.)
 *   ElementSnapshot        frozen copy of an element's fields, used by the
 *                          change record to roll state forwards and backwards
 *   CanvasView             contract the records use to tell the view to
 *                          add, remove, or repaint on undo/redo, so the
 *                          model stays free of any view.* import
 *
 * The view package reads these types to draw the card and implements
 * CanvasView; the controller package mutates them on user input.
 */
package com.mycompany.cardcreator.model;
