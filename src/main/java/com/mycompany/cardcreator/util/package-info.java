/**
 * Shared helpers that don't belong to a specific screen.
 *
 * Most of what lives here is the undo/redo system:
 *   ActionsManager         keeps the undo and redo stacks, merges rapid
 *                          changes on the same element into one step
 *   ElementRecord          interface every undoable action implements
 *   AddedElementRecord     element was spawned; undo removes it, redo puts it back
 *   DeletedElementRecord   element was deleted; undo puts it back, redo removes it
 *   ChangedElementRecord   element was changed (moved, resized, recolored, etc.)
 *   BackgroundImageRecord  background image was dragged or resized
 *   ElementSnapshot        frozen copy of an element's fields, used by the
 *                          change record to roll state forwards and backwards
 *
 * Plus:
 *   SoundPlayer  plays UI click and cue sounds from the classpath
 *
 * Every action a user takes that should be undoable ends up as one of these
 * records on the ActionsManager.
 */
package com.mycompany.cardcreator.util;
