package com.mycompany.cardcreator.util;

import java.util.ArrayDeque;
import java.util.Deque;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.view.CardCanvas;

/**
 * Session-scoped undo/redo stack for the card editor.  fresh slate each
 * time the editor opens so yesterday's edits never show up in today's session.
 *
 * Mutations on the same element merge into a single undo step when they
 * happen within MERGE_WINDOW_MS of each other -- lets holding a spinner
 * arrow or clicking through palette swatches count as one action.
 */
public class ActionsManager {

    private static final long MERGE_WINDOW_MS = 800;

    private final Deque<ElementRecord> undoStack = new ArrayDeque<>();
    private final Deque<ElementRecord> redoStack = new ArrayDeque<>();

    // push for adds/deletes. user-initiated action so wipe redo stack
    public void record(ElementRecord r) {
        undoStack.push(r);
        redoStack.clear();
    }

    // push for mutations, with merge. if the top of the undo stack is a
    // ChangedElementRecord for the same element within the window, we extend
    // it instead of pushing a new record. a no action change (before==after) is
    // dropped entirely
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

    public void undo() {
        if (undoStack.isEmpty()) return;
        ElementRecord r = undoStack.pop();
        r.undo();
        redoStack.push(r);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        ElementRecord r = redoStack.pop();
        r.redo();
        undoStack.push(r);
    }

    public boolean canUndo(){
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
