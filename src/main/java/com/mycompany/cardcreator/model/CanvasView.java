package com.mycompany.cardcreator.model;

/**
 * View surface an undo record needs to talk to when it rolls state back
 * or forward.
 *
 * The undo records live in the model package so they stay on the model
 * side of MVC. They still need to tell the view to add, remove, or
 * repaint elements. This interface is that contract: records depend on
 * it; the CardCanvas in the view package implements it.
 */
public interface CanvasView {

    /**
     * Adds the given element to the canvas and makes it the selection.
     *
     * @param el the element to add
     */
    void addElement(CardElement el);

    /**
     * Removes the given element from the canvas.
     *
     * @param el the element to remove
     */
    void removeElement(CardElement el);

    /**
     * Requests a repaint of the canvas.
     */
    void repaint();
}
