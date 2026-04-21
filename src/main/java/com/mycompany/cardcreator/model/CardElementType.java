package com.mycompany.cardcreator.model;

/**
 * The kinds of things a user can put on a card.
 *
 * Acts as the discriminator on CardElement so renderer and toolbox code can
 * branch on what shape to draw or what controls to show.
 */
public enum CardElementType {
    /** Text block with a font size, color, and rotation. */
    TEXT,

    /** Axis-aligned rectangle, either outlined or filled. */
    RECTANGLE,

    /** Circle/ellipse inscribed in the element's bounding box. */
    CIRCLE,

    /** Bitmap image loaded from a path relative to the project folder. */
    IMAGE
}
