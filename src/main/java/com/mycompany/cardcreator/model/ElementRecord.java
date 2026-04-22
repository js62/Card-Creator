package com.mycompany.cardcreator.model;

/**
 * One step in the undo history.
 *
 * Every user action that should be undoable is represented as a single
 * ElementRecord and pushed onto the ActionsManager. Calling undo puts the
 * project back the way it was before the action; calling redo puts it
 * back the way it was after.
 */
public interface ElementRecord {

    /**
     * Rolls the project back to the state before this action happened.
     */
    void undo();

    /**
     * Reapplies this action to the project.
     */
    void redo();
}
