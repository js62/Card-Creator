package com.mycompany.cardcreator.util;

import com.mycompany.cardcreator.view.CardCanvas;

/**
 * Undo record for "user dragged or resized the background image".
 *
 * Holds the image geometry before and after the gesture. CardCanvas hands
 * over the before values when the mouse is pressed on the image and the
 * after values when the mouse is released, so one drag or resize becomes
 * one undo step.
 */
public class BackgroundImageRecord implements ElementRecord {

    private final CardCanvas canvas;
    private final int beforeX, beforeY, beforeW, beforeH;
    private final int afterX, afterY, afterW, afterH;

    /**
     * Builds a record tracking a background image drag or resize.
     *
     * @param canvas   the CardCanvas that owns the background image
     * @param beforeX  image x before the gesture
     * @param beforeY  image y before the gesture
     * @param beforeW  image width before the gesture
     * @param beforeH  image height before the gesture
     * @param afterX   image x after the gesture
     * @param afterY   image y after the gesture
     * @param afterW   image width after the gesture
     * @param afterH   image height after the gesture
     */
    public BackgroundImageRecord(CardCanvas canvas,
        int beforeX, int beforeY, int beforeW, int beforeH,
        int afterX, int afterY, int afterW, int afterH)
    {
        this.canvas = canvas;
        this.beforeX = beforeX;
        this.beforeY = beforeY;
        this.beforeW = beforeW;
        this.beforeH = beforeH;
        this.afterX = afterX;
        this.afterY = afterY;
        this.afterW = afterW;
        this.afterH = afterH;
    }

    /**
     * Returns true when the gesture ended exactly where it started.
     *
     * A caller can drop the record instead of adding a no change step to
     * the stack.
     *
     * @return true if before and after geometry match
     */
    public boolean isNoOp() {
        return beforeX == afterX && beforeY == afterY
            && beforeW == afterW && beforeH == afterH;
    }

    /**
     * Puts the background image back at the geometry it had before the
     * gesture.
     */
    @Override
    public void undo() {
        canvas.setImageBounds(beforeX, beforeY, beforeW, beforeH);
    }

    /**
     * Puts the background image back at the geometry it had after the
     * gesture.
     */
    @Override
    public void redo() {
        canvas.setImageBounds(afterX, afterY, afterW, afterH);
    }
}
