package com.mycompany.cardcreator.util;

import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.view.CardCanvas;

/**
 * Undo record for "user changed an element".
 *
 * Holds a before and after snapshot of the same element. The before
 * snapshot is taken when the user starts editing; the after snapshot is
 * updated as the edit goes on, so rapid tweaks (holding a spinner arrow,
 * clicking through palette swatches) all merge into one undo step. See
 * ActionsManager.recordChange for the merging rules.
 */
public class ChangedElementRecord implements ElementRecord {

    private final CardElement element;
    private final CardCanvas canvas;
    private final ElementSnapshot before;
    private ElementSnapshot after;
    private long lastUpdatedMillis;

    /**
     * Builds a record tracking a change to the given element.
     *
     * @param element the element that was changed
     * @param canvas  the CardCanvas the element is on, repainted on undo and redo
     * @param before  the state of the element before the change
     * @param after   the state of the element after the change
     */
    public ChangedElementRecord(CardElement element, CardCanvas canvas,
                                ElementSnapshot before, ElementSnapshot after){
        this.element =element;
        this.canvas=canvas;
        this.before =before;
        this.after=after;
        this.lastUpdatedMillis = System.currentTimeMillis();
    }

    /**
     * Returns the element this record is tracking.
     *
     * Used by ActionsManager to check whether a new change belongs to the
     * same element as the one on top of the stack.
     *
     * @return the tracked element
     */
    public CardElement getElement() {
        return element;
    }

    /**
     * Returns the timestamp of the last time this record's after state
     * was updated, in milliseconds since the epoch.
     *
     * @return the last update time
     */
    public long getLastUpdatedMillis(){
        return lastUpdatedMillis;
    }

    /**
     * Updates the after state and bumps the last updated timestamp.
     *
     * Called by ActionsManager when a later change on the same element
     * lands inside the merge window. The before snapshot is kept; only
     * after slides forward.
     *
     * @param newAfter the new after state
     */
    public void updateAfter(ElementSnapshot newAfter) {
        this.after = newAfter;
        this.lastUpdatedMillis = System.currentTimeMillis();
    }

    /**
     * Returns true if the user ended up back where they started.
     *
     * A caller can drop the record instead of pushing a no change step
     * onto the stack.
     *
     * @return true when before and after match field for field
     */
    public boolean isNoOp() {
        return before.equalsState(after);
    }

    /**
     * Writes the before snapshot onto the element and repaints the canvas.
     */
    @Override
    public void undo() {
        before.applyTo(element);
        canvas.repaint();
    }

    /**
     * Writes the after snapshot onto the element and repaints the canvas.
     */
    @Override
    public void redo() {
        after.applyTo(element);
        canvas.repaint();
    }
}
