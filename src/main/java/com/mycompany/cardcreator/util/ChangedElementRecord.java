package com.mycompany.cardcreator.util;

import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.view.CardCanvas;

// records a mutation on a single element. the eforesnapshot is captured
// right before the user starts editing; after is updated as the edit
// progresses so rapid tweaks (spinner held down, palette clicks in a row)
// coalesce into one undo step. see ActionsManager.recordChange for the
// coalescing rules.
public class ChangedElementRecord implements ElementRecord {

    private final CardElement element;
    private final CardCanvas canvas;
    private final ElementSnapshot before;
    private ElementSnapshot after;
    private long lastUpdatedMillis;

    public ChangedElementRecord(CardElement element, CardCanvas canvas,
                                ElementSnapshot before, ElementSnapshot after){
        this.element =element;
        this.canvas=canvas;
        this.before =before;
        this.after=after;
        this.lastUpdatedMillis = System.currentTimeMillis();
    }

    public CardElement getElement() {
        return element;
    }

    public long getLastUpdatedMillis(){
        return lastUpdatedMillis;
    }

    // called by ActionsManager when a later change on the same element lands
    // inside the coalesce window -- we keep `before` but slide `after` forward
    public void updateAfter(ElementSnapshot newAfter) {
        this.after = newAfter;
        this.lastUpdatedMillis = System.currentTimeMillis();
    }

    // true if the user ended up back where they started -- caller can drop
    // the record instead of pushing a no-op onto the stack
    public boolean isNoOp() {
        return before.equalsState(after);
    }

    @Override
    public void undo() {
        before.applyTo(element);
        canvas.repaint();
    }

    @Override
    public void redo() {
        after.applyTo(element);
        canvas.repaint();
    }
}
