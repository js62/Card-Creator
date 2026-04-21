package com.mycompany.cardcreator.util;

import com.mycompany.cardcreator.model.CardElement;

/**
 * Frozen copy of every editable field on a CardElement.
 *
 * Used by ChangedElementRecord to put state back the way it was on undo
 * and the way it was on redo. Two snapshots of the same state compare
 * equal through equalsState.
 */
public class ElementSnapshot {

    /** Position and size copied from the element. */
    public final int x, y, width, height;

    /** Text content copied from the element. */
    public final String text;

    /** Font size copied from the element. */
    public final int fontSize;

    /** Color as "#rrggbb", copied from the element. */
    public final String colorHex;

    /** Fill flag copied from the element. */
    public final boolean filled;

    /** Rotation in degrees, copied from the element. */
    public final double rotation;

    /** Image path copied from the element. */
    public final String imagePath;

    /** Layer ordering copied from the element. */
    public final int zLayer;

    /**
     * Captures every editable field on the given element.
     *
     * @param el the element to snapshot; must not be null
     */
    public ElementSnapshot(CardElement el) {
        this.x=el.x;
        this.y=el.y;
        this.width=el.width;
        this.height=el.height;
        this.text = el.text;
        this.fontSize = el.fontSize;
        this.colorHex=el.colorHex;
        this.filled = el.filled;
        this.rotation=el.rotation;
        this.imagePath = el.imagePath;
        this.zLayer=el.zLayer;
    }

    /**
     * Writes every field on this snapshot back onto the given element.
     *
     * Used by the undo system to put an element back the way it was.
     *
     * @param el the element to write into
     */
    public void applyTo(CardElement el) {
        el.x=x;
        el.y=y;
        el.width = width;
        el.height=height;
        el.text = text;
        el.fontSize=fontSize;
        el.colorHex = colorHex;
        el.filled=filled;
        el.rotation = rotation;
        el.imagePath=imagePath;
        el.zLayer = zLayer;
    }

    /**
     * Returns true when this snapshot matches the other field for field.
     *
     * ActionsManager uses this to drop no change mutations: if the user
     * pressed then released the mouse without moving, the before and
     * after snapshots match and nothing needs to be saved.
     *
     * @param other the snapshot to compare against
     * @return true if every field matches
     */
    public boolean equalsState(ElementSnapshot other) {
        return x==other.x
            && y == other.y
            && width==other.width
            && height == other.height
            && fontSize==other.fontSize
            && filled == other.filled
            && Double.compare(rotation,other.rotation)==0
            && zLayer == other.zLayer
            && java.util.Objects.equals(text,other.text)
            && java.util.Objects.equals(colorHex, other.colorHex)
            && java.util.Objects.equals(imagePath,other.imagePath);
    }
}
