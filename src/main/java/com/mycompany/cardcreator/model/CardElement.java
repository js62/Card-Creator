package com.mycompany.cardcreator.model;

import java.awt.Color;
import java.awt.Font;
import java.util.UUID;

/**
 * One item placed on a card: text, shape, or image.
 *
 * All properties for every element type are kept on the same class so the
 * whole element can be saved as one json object. Unused fields (like
 * fontSize on a rectangle) just stay at their defaults.
 */
public class CardElement {

    /** Unique id assigned by the Model when this element is added. */
    public UUID id;

    /** What kind of element this is; picks which fields matter. */
    public CardElementType type;

    /** Position and size of the element's bounding box on the card. */
    public int x, y, width, height;

    /** Text content, used only when type == TEXT. */
    public String text;

    /** Point size for the TEXT font. */
    public int fontSize;

    /** Color in "#rrggbb" form so it can be written straight to json. */
    public String colorHex;

    /** Whether a shape is filled (true) or stroked only (false). */
    public boolean filled;

    /** Rotation around the element's center, in degrees. */
    public double rotation;

    /** Path to the source file for an IMAGE element, relative to the project folder. */
    public String imagePath;

    /** Render order; higher values draw on top of lower ones. */
    public int zLayer;

    /**
     * No-arg constructor used when loading from json.
     */
    public CardElement() {}

    /**
     * Creates a new element with defaults set for the given type.
     *
     * The text is empty, the font size is 24, the color is black, shapes
     * start unfilled, rotation is zero, and the layer is zero. Callers
     * override whichever of those they care about.
     *
     * @param type   what kind of element this is
     * @param x      left edge of the element in card coordinates
     * @param y      top edge of the element in card coordinates
     * @param width  width of the element in card coordinates
     * @param height height of the element in card coordinates
     */
    public CardElement(CardElementType type, int x, int y, int width, int height){
        this.type=type;
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.text="";
        this.fontSize=24;
        this.colorHex="#000000";
        this.filled=false;
        this.rotation=0;
        this.imagePath=null;
        this.zLayer=0;
    }

    /**
     * Returns an AWT Font for drawing this element's text at the current size.
     *
     * @return a sans-serif plain Font at the element's fontSize
     */
    public Font getFont(){
        return new Font("SansSerif", Font.PLAIN, fontSize);
    }

    /**
     * Returns the element's color as an AWT Color.
     *
     * Throws NumberFormatException if colorHex isn't a valid "#rrggbb"
     * string, which only happens with a corrupt save file.
     *
     * @return the color decoded from colorHex
     */
    public Color getColor(){
        return Color.decode(colorHex);
    }

    /**
     * Stores the given color as a "#rrggbb" string on this element.
     *
     * @param c the color to store
     */
    public void setColor(Color c){
        colorHex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
