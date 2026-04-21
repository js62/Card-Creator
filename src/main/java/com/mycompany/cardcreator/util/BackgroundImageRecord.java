package com.mycompany.cardcreator.util;

import com.mycompany.cardcreator.view.CardCanvas;

// captures a background image drag or resize gesture. CardCanvas hands us the
// pre-gesture geometry on mousePressed and the post-gesture geometry on
// mouseReleased so one gesture = one undo step
public class BackgroundImageRecord implements ElementRecord {

    private final CardCanvas canvas;
    private final int beforeX, beforeY, beforeW, beforeH;
    private final int afterX, afterY, afterW, afterH;

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

    public boolean isNoOp() {
        return beforeX == afterX && beforeY == afterY
            && beforeW == afterW && beforeH == afterH;
    }

    @Override
    public void undo() {
        canvas.setImageBounds(beforeX, beforeY, beforeW, beforeH);
    }

    @Override
    public void redo() {
        canvas.setImageBounds(afterX, afterY, afterW, afterH);
    }
}
