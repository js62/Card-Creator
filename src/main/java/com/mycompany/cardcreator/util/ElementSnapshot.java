package com.mycompany.cardcreator.util;

import com.mycompany.cardcreator.model.CardElement;

// frozen copy of every user-editable field on a CardElement.
// used by ChangedElementRecord to restore state on undo/redo.
public class ElementSnapshot {

    public final int x, y, width, height;
    public final String text;
    public final int fontSize;
    public final String colorHex;
    public final boolean filled;
    public final double rotation;
    public final String imagePath;
    public final int zLayer;

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

    // true when nothing actually changed between two snapshots,lets callers
    // skip recording no change mutations (mousePressed then mouseReleased
    // without any drag in between)
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
