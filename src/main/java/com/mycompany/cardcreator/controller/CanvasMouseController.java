package com.mycompany.cardcreator.controller;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.CardElementType;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.model.ActionsManager;
import com.mycompany.cardcreator.model.DeletedElementRecord;
import com.mycompany.cardcreator.model.ElementSnapshot;
import com.mycompany.cardcreator.view.CardCanvas;

/**
 * Mouse handler for a CardCanvas.
 *
 * Listens for clicks and drags on the canvas and turns them into
 * selection, drag, resize, and right click actions. The start of each
 * gesture is remembered on mousePressed and pushed onto the
 * ActionsManager on mouseReleased, so one drag or resize is one undo
 * step.
 */
public class CanvasMouseController extends MouseAdapter {

    private final CardCanvas canvas;
    private final Model model;
    private final UUID cardID;
    private final ActionsManager actions;

    // element drag / resize state
    private boolean draggingElement = false;
    private boolean resizingElement = false;
    private int elementResizeCorner = -1;
    private int elementDragOffsetX, elementDragOffsetY;
    private ElementSnapshot gestureBefore = null;
    private CardElement gestureElement = null;

    /**
     * Builds a mouse controller that listens on one canvas.
     *
     * @param canvas  the CardCanvas to listen on
     * @param model   the project Model, used for deleting elements
     * @param cardID  id of the card being edited
     * @param actions the undo and redo stack for this editing session
     */
    public CanvasMouseController(CardCanvas canvas, Model model, UUID cardID, ActionsManager actions) {
        this.canvas=canvas;
        this.model=model;
        this.cardID=cardID;
        this.actions=actions;
    }

    /**
     * Handles a mouse button press on the canvas.
     *
     * Right clicks open the context menu. Left clicks either grab a
     * resize handle on the selected element or start dragging whichever
     * element is under the cursor.
     *
     * @param e the incoming MouseEvent
     */
    @Override
    public void mousePressed(MouseEvent e) {
        int mx = canvas.toCardX(e.getX());
        int my = canvas.toCardY(e.getY());

        if (SwingUtilities.isRightMouseButton(e)){
            handleRightClick(e, mx, my);
            return;
        }

        // check if we're grabbing a resize handle on the selected element
        CardElement sel = canvas.getSelectedElement();
        if (sel != null) {
            int corner = getElementCornerAt(sel, mx, my);
            if (corner >= 0) {
                resizingElement = true;
                elementResizeCorner = corner;
                gestureElement = sel;
                gestureBefore = new ElementSnapshot(sel);
                return;
            }
        }

        // click inside any element (iterate top to bottom)
        var elements = canvas.getElements();
        for (int i = elements.size() - 1; i >= 0; i--)
        {
            CardElement el = elements.get(i);
            if (mx >= el.x && mx <= el.x + el.width
                    && my >= el.y && my <= el.y + el.height) {
                canvas.setSelectedElement(el);
                draggingElement = true;
                elementDragOffsetX = mx - el.x;
                elementDragOffsetY = my - el.y;
                gestureElement = el;
                gestureBefore = new ElementSnapshot(el);
                return;
            }
        }

        // nothing hit, deselect
        canvas.setSelectedElement(null);
    }

    /**
     * Handles a mouse button release on the canvas.
     *
     * Snaps the element to the grid, pushes the gesture onto the undo
     * stack as a single step, and clears the gesture state.
     *
     * @param e the incoming MouseEvent
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        boolean endedGesture = false;
        CardElement sel = canvas.getSelectedElement();

        // snap element to grid on release
        if (draggingElement && sel != null) {
            sel.x = canvas.snapToGrid(sel.x);
            sel.y = canvas.snapToGrid(sel.y);
            draggingElement = false;
            endedGesture = true;
            canvas.repaint();
        }
        if (resizingElement && sel != null) {
            sel.x = canvas.snapToGrid(sel.x);
            sel.y = canvas.snapToGrid(sel.y);
            sel.width = Math.max(CardCanvas.GRID_SIZE, canvas.snapToGrid(sel.width));
            sel.height = Math.max(CardCanvas.GRID_SIZE, canvas.snapToGrid(sel.height));
            resizingElement = false;
            elementResizeCorner = -1;
            endedGesture = true;
            canvas.repaint();
        }

        // commit the drag/resize to undo stack. recordChange drops no-op
        // changes internally so a click-without-drag wont clutter the stack
        if (endedGesture && gestureElement != null && gestureBefore != null) {
            actions.recordChange(gestureElement, canvas,
                gestureBefore, new ElementSnapshot(gestureElement));
        }
        gestureElement = null;
        gestureBefore = null;
    }

    /**
     * Handles a mouse drag on the canvas.
     *
     * Moves the element being dragged or resizes the element whose
     * handle was grabbed, depending on what mousePressed started.
     *
     * @param e the incoming MouseEvent
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        int mx = canvas.toCardX(e.getX());
        int my = canvas.toCardY(e.getY());
        CardElement sel = canvas.getSelectedElement();

        if (draggingElement && sel != null) {
            sel.x = mx - elementDragOffsetX;
            sel.y = my - elementDragOffsetY;
            canvas.repaint();
        } else if (resizingElement && sel != null) {
            resizeElement(sel, elementResizeCorner, mx, my);
            canvas.repaint();
        }
    }

    /**
     * Handles mouse movement over the canvas with no button pressed.
     *
     * Updates the cursor so the user can tell whether the pointer is on
     * a resize handle, over a movable element, or over empty space.
     *
     * @param e the incoming MouseEvent
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        int mx = canvas.toCardX(e.getX());
        int my = canvas.toCardY(e.getY());
        CardElement sel = canvas.getSelectedElement();

        // element resize handles first
        if (sel != null) {
            int corner = getElementCornerAt(sel, mx, my);
            if (corner == 0 || corner == 3) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                return;
            } else if (corner == 1 || corner == 2) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                return;
            }
        }

        // hovering over any element
        for (CardElement el : canvas.getElements()) {
            if (mx >= el.x && mx <= el.x + el.width
                    && my >= el.y && my <= el.y + el.height) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                return;
            }
        }

        canvas.setCursor(Cursor.getDefaultCursor());
    }

    // right click menu: delete + (for shapes) fill/unfill
    private void handleRightClick(MouseEvent e, int mx, int my) {
        var elements = canvas.getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            CardElement el = elements.get(i);
            
            if (mx >= el.x && mx <= el.x + el.width
                    && my >= el.y && my <= el.y + el.height) {
                canvas.setSelectedElement(el);

                JPopupMenu popup = new JPopupMenu();

                JMenuItem deleteItem = new JMenuItem("Delete");
                deleteItem.addActionListener(a -> {
                    
                    // record first so the DeletedElementRecord still has the
                    // model/canvas linkage it needs for redo
                    actions.record(new DeletedElementRecord(model, canvas, cardID, el));
                    canvas.removeElement(el);
                    if (el.id != null) {
                        model.removeCardElement(cardID, el.id);
                    }
                });
                popup.add(deleteItem);

                if (el.type != CardElementType.TEXT && el.type != CardElementType.IMAGE) {
                    JMenuItem fillItem = new JMenuItem(el.filled ? "Unfill" : "Fill");
                    fillItem.addActionListener(a -> {
                        ElementSnapshot before = new ElementSnapshot(el);
                        el.filled = !el.filled;
                        actions.recordChange(el, canvas, before, new ElementSnapshot(el));
                        canvas.repaint();
                    });
                    popup.add(fillItem);
                }

                popup.show(canvas, e.getX(), e.getY());
                canvas.repaint();
                return;
            }
        }
    }

    // corner resize for an element
    private void resizeElement(CardElement el, int corner, int mx, int my) {
        int g = CardCanvas.GRID_SIZE;
        switch (corner) {
            case 0: { // top-left
                int nw = (el.x + el.width) - mx;
                int nh = (el.y + el.height) - my;
                if (nw > g && nh > g) {
                    el.x = mx; el.y = my; el.width = nw; el.height = nh;
                }
                break;
            }
            case 1: { // top-right
                int nw = mx - el.x;
                int nh = (el.y + el.height) - my;
                if (nw > g && nh > g) {
                    el.y = my; el.width = nw; el.height = nh;
                }
                break;
            }
            case 2: { // bottom-left
                int nw = (el.x + el.width) - mx;
                int nh = my - el.y;
                if (nw > g && nh > g) {
                    el.x = mx; el.width = nw; el.height = nh;
                }
                break;
            }
            case 3: { // bottom-right
                int nw = mx - el.x;
                int nh = my - el.y;
                if (nw > g && nh > g) {
                    el.width = nw; el.height = nh;
                }
                break;
            }
        }
    }

    // is (mx,my) near a corner handle of the selected element?
    private int getElementCornerAt(CardElement el, int mx, int my) {
        int half = CardCanvas.HANDLE_SIZE / 2;
        if (Math.abs(mx - el.x) <= half && Math.abs(my - el.y) <= half) return 0;
        if (Math.abs(mx - (el.x + el.width)) <= half && Math.abs(my - el.y) <= half) return 1;
        if (Math.abs(mx - el.x) <= half && Math.abs(my - (el.y + el.height)) <= half) return 2;
        if (Math.abs(mx - (el.x + el.width)) <= half && Math.abs(my - (el.y + el.height)) <= half) return 3;
        return -1;
    }
}
