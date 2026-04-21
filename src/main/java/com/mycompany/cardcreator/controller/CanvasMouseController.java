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
import com.mycompany.cardcreator.util.ActionsManager;
import com.mycompany.cardcreator.util.BackgroundImageRecord;
import com.mycompany.cardcreator.util.DeletedElementRecord;
import com.mycompany.cardcreator.util.ElementSnapshot;
import com.mycompany.cardcreator.view.CardCanvas;

// routes mouse input on a CardCanvas into selection, drag, resize, and right
// click actions. each gesture is captured on mousePressed and committed to
// the ActionsManager on mouseReleased so one gesture == one undo step
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

    // background image drag state
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    // background image resize state
    private boolean resizing = false;
    private int resizeCorner = -1;
    private float aspectRatio = 1f;

    // snapshot of the background image geometry when a gesture started
    private int bgBeforeX, bgBeforeY, bgBeforeW, bgBeforeH;
    private boolean bgGestureActive = false;

    public CanvasMouseController(CardCanvas canvas, Model model, UUID cardID, ActionsManager actions) {
        this.canvas = canvas;
        this.model = model;
        this.cardID = cardID;
        this.actions = actions;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = canvas.toCardX(e.getX());
        int my = canvas.toCardY(e.getY());

        if (SwingUtilities.isRightMouseButton(e)) {
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
        for (int i = elements.size() - 1; i >= 0; i--) {
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

        // background image handling
        if (canvas.getBackgroundImage() != null) {
            int corner = getBackgroundCornerAt(mx, my);
            if (corner >= 0) {
                resizing = true;
                resizeCorner = corner;
                aspectRatio = (float) canvas.getImgW() / canvas.getImgH();
                captureBackgroundGestureStart();
                return;
            }
            if (mx >= canvas.getImgX() && mx <= canvas.getImgX() + canvas.getImgW()
                    && my >= canvas.getImgY() && my <= canvas.getImgY() + canvas.getImgH()) {
                dragging = true;
                dragOffsetX = mx - canvas.getImgX();
                dragOffsetY = my - canvas.getImgY();
                captureBackgroundGestureStart();
            }
        }
    }

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

        // snap background image to grid
        boolean endedBgGesture = false;
        if (dragging) {
            canvas.setImageBounds(canvas.snapToGrid(canvas.getImgX()),
                canvas.snapToGrid(canvas.getImgY()),
                canvas.getImgW(), canvas.getImgH());
            dragging = false;
            endedBgGesture = true;
        }
        if (resizing) {
            canvas.setImageBounds(canvas.snapToGrid(canvas.getImgX()),
                canvas.snapToGrid(canvas.getImgY()),
                Math.max(CardCanvas.GRID_SIZE, canvas.snapToGrid(canvas.getImgW())),
                Math.max(CardCanvas.GRID_SIZE, canvas.snapToGrid(canvas.getImgH())));
            resizing = false;
            resizeCorner = -1;
            endedBgGesture = true;
        }
        if (endedBgGesture && bgGestureActive) {
            commitBackgroundGesture();
        }
        bgGestureActive = false;
    }

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
        } else if (resizing && canvas.getBackgroundImage() != null) {
            resizeBackgroundImage(resizeCorner, mx, my);
        } else if (dragging) {
            canvas.setImageBounds(mx - dragOffsetX, my - dragOffsetY,
                canvas.getImgW(), canvas.getImgH());
        }
    }

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

        // background image handles / body
        if (canvas.getBackgroundImage() != null) {
            int corner = getBackgroundCornerAt(mx, my);
            if (corner == 0 || corner == 3) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            } else if (corner == 1 || corner == 2) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
            } else if (mx >= canvas.getImgX() && mx <= canvas.getImgX() + canvas.getImgW()
                    && my >= canvas.getImgY() && my <= canvas.getImgY() + canvas.getImgH()) {
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else {
                canvas.setCursor(Cursor.getDefaultCursor());
            }
        } else {
            canvas.setCursor(Cursor.getDefaultCursor());
        }
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

    // corner resize for the background image, maintaining aspect ratio
    private void resizeBackgroundImage(int corner, int mx, int my) {
        int ix = canvas.getImgX();
        int iy = canvas.getImgY();
        int iw = canvas.getImgW();
        int ih = canvas.getImgH();
        int g = CardCanvas.GRID_SIZE;
        switch (corner) {
            case 0: {
                int nw = (ix + iw) - mx;
                int nh = Math.round(nw / aspectRatio);
                if (nw > g) canvas.setImageBounds((ix + iw) - nw, (iy + ih) - nh, nw, nh);
                break;
            }
            case 1: {
                int nw = mx - ix;
                int nh = Math.round(nw / aspectRatio);
                if (nw > g) canvas.setImageBounds(ix, (iy + ih) - nh, nw, nh);
                break;
            }
            case 2: {
                int nw = (ix + iw) - mx;
                int nh = Math.round(nw / aspectRatio);
                if (nw > g) canvas.setImageBounds((ix + iw) - nw, iy, nw, nh);
                break;
            }
            case 3: {
                int nw = mx - ix;
                int nh = Math.round(nw / aspectRatio);
                if (nw > g) canvas.setImageBounds(ix, iy, nw, nh);
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

    // is (mx,my) near a corner handle of the background image?
    private int getBackgroundCornerAt(int mx, int my) {
        int half = CardCanvas.HANDLE_SIZE / 2;
        int x = canvas.getImgX(), y = canvas.getImgY();
        int w = canvas.getImgW(), h = canvas.getImgH();
        if (Math.abs(mx - x) <= half && Math.abs(my - y) <= half) return 0;
        if (Math.abs(mx - (x + w)) <= half && Math.abs(my - y) <= half) return 1;
        if (Math.abs(mx - x) <= half && Math.abs(my - (y + h)) <= half) return 2;
        if (Math.abs(mx - (x + w)) <= half && Math.abs(my - (y + h)) <= half) return 3;
        return -1;
    }

    private void captureBackgroundGestureStart() {
        bgBeforeX = canvas.getImgX();
        bgBeforeY = canvas.getImgY();
        bgBeforeW = canvas.getImgW();
        bgBeforeH = canvas.getImgH();
        bgGestureActive = true;
    }

    private void commitBackgroundGesture() {
        BackgroundImageRecord rec = new BackgroundImageRecord(canvas,
            bgBeforeX, bgBeforeY, bgBeforeW, bgBeforeH,
            canvas.getImgX(), canvas.getImgY(), canvas.getImgW(), canvas.getImgH());
        if (!rec.isNoOp()) {
            actions.record(rec);
        }
    }
}
