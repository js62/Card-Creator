package com.mycompany.cardcreator.util;

import java.util.ArrayDeque;
import java.util.Deque;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.view.CardCanvas;

/**
 * Undo and redo stacks for the card editor.
 *
 * An ActionsManager keeps two stacks: a list of things that can be undone
 * and a list of things that can be redone. A fresh ActionsManager is made
 * each time the editor opens, so edits from yesterday never show up in
 * today's session.
 *
 * Changes that happen to the same element inside a short window merge
 * into one undo step, so holding a spinner arrow or clicking through
 * several palette swatches counts as one action instead of dozens.
 */
public class ActionsManager {

    /** Changes on the same element within this many milliseconds merge. */
    private static final long MERGE_WINDOW_MS = 800;

    private final Deque<ElementRecord> undoStack = new ArrayDeque<>();
    private final Deque<ElementRecord> redoStack = new ArrayDeque<>();

    /**
     * Pushes an add or delete onto the undo stack.
     *
     * The redo stack is cleared because a user action has just happened,
     * so any "future" that was in the redo stack is no longer reachable.
     *
     * @param r the record to push
     */
    public void record(ElementRecord r) {
        undoStack.push(r);
        redoStack.clear();
    }

    /**
     * Pushes a change onto the undo stack, merging when it makes sense.
     *
     * If the top of the undo stack is already a ChangedElementRecord for
     * the same element and the previous update was less than the merge
     * window ago, the existing record's after state is bumped forward
     * instead of a new record being pushed. A change where before equals
     * after is dropped entirely.
     *
     * The redo stack is cleared on every call because a user action has
     * just happened.
     *
     * @param el      the element being changed
     * @param canvas  the CardCanvas to repaint on undo and redo
     * @param before  the element's state before the change
     * @param after   the element's state after the change
     */
    public void recordChange(CardElement el, CardCanvas canvas,
                             ElementSnapshot before, ElementSnapshot after) {
        if (before.equalsState(after)) {
            return;
        }

        ElementRecord top = undoStack.peek();
        long now = System.currentTimeMillis();
        if (top instanceof ChangedElementRecord c
                && c.getElement()==el
                && now - c.getLastUpdatedMillis() < MERGE_WINDOW_MS) {
            c.updateAfter(after);
            redoStack.clear();
            return;
        }

        undoStack.push(new ChangedElementRecord(el, canvas, before, after));
        redoStack.clear();
    }

    /**
     * Pops the top record off the undo stack, undoes it, and pushes it
     * onto the redo stack.
     *
     * Does nothing when there is no record to undo.
     */
    public void undo() {
        if (undoStack.isEmpty()) return;
        ElementRecord r = undoStack.pop();
        r.undo();
        redoStack.push(r);
    }

    /**
     * Pops the top record off the redo stack, redoes it, and pushes it
     * back onto the undo stack.
     *
     * Does nothing when there is no record to redo.
     */
    public void redo() {
        if (redoStack.isEmpty()) return;
        ElementRecord r = redoStack.pop();
        r.redo();
        undoStack.push(r);
    }

    /**
     * Returns true when at least one action can be undone.
     *
     * @return true if the undo stack has anything on it
     */
    public boolean canUndo(){
        return !undoStack.isEmpty();
    }

    /**
     * Returns true when at least one action can be redone.
     *
     * @return true if the redo stack has anything on it
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
