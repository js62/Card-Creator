package com.mycompany.cardcreator.view;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mycompany.cardcreator.controller.CanvasMouseController;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.Model;
import com.mycompany.cardcreator.util.ActionsManager;

/**
 * The drawing surface inside the editor for one card.
 *
 * A CardCanvas holds the state for a single card: the elements, the
 * selected element, and the background image with its position and size.
 * Painting is handed off to CardRenderer and mouse input is handed off to
 * CanvasMouseController, so this class stays focused on state and on the
 * public methods those two collaborators need.
 */
public class CardCanvas extends JPanel {

    /** Grid spacing in card pixels; elements snap to multiples of this on drop. */
    public static final int GRID_SIZE = 25;

    /** Width and height of the square selection handles drawn at each corner. */
    public static final int HANDLE_SIZE = 10;

    private final int canvasWidth;
    private final int canvasHeight;

    private BufferedImage backgroundImage;
    private int imgX = 0;
    private int imgY = 0;
    private int imgW;
    private int imgH;

    private final List<CardElement> elements = new ArrayList<>();
    private CardElement selectedElement = null;

    private final CardRenderer renderer;
    private Runnable onSelectionChanged = null;


    /**
     * Builds a canvas for one card at the given page size.
     *
     * The background image starts covering the whole card. A new
     * CardRenderer and a new CanvasMouseController are created and
     * wired up to this canvas.
     *
     * @param model        the project Model being edited
     * @param cardID       id of the card this canvas is editing
     * @param canvasWidth  card width in pixels
     * @param canvasHeight card height in pixels
     * @param actions      the undo and redo stack for this editing session
     */
    public CardCanvas(Model model, UUID cardID, int canvasWidth, int canvasHeight, ActionsManager actions) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.imgW = canvasWidth;
        this.imgH = canvasHeight;
        setBackground(Color.WHITE);

        this.renderer = new CardRenderer(model);

        CanvasMouseController mouse = new CanvasMouseController(this, model, cardID, actions);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }


    /**
     * Registers a callback that runs whenever the selected element changes.
     *
     * The Toolbox uses this to keep its widgets in sync with whatever
     * element the user clicked on.
     *
     * @param callback the runnable to invoke on selection changes
     */
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback;
    }

    private void fireSelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.run();
        }
    }

    /**
     * Returns the element the user currently has selected, or null if
     * nothing is selected.
     *
     * @return the selected CardElement, or null
     */
    public CardElement getSelectedElement() {
        return selectedElement;
    }

    /**
     * Picks a new selected element, fires the selection callback, and
     * repaints.
     *
     * Passing null clears the selection.
     *
     * @param el the element to select, or null to deselect everything
     */
    public void setSelectedElement(CardElement el) {
        this.selectedElement = el;
        fireSelectionChanged();
        repaint();
    }

    /**
     * Clears the selection.
     *
     * Same as calling setSelectedElement(null).
     */
    public void clearSelection() {
        setSelectedElement(null);
    }


    /**
     * Returns the list of elements on this card, in the order they were added.
     *
     * @return the live element list (not a copy)
     */
    public List<CardElement> getElements() {
        return elements;
    }

    /**
     * Adds the given element to this card and selects it.
     *
     * Used both when the user spawns a new element and when the undo
     * system puts a deleted element back.
     *
     * @param el the element to add
     */
    public void addElement(CardElement el) {
        elements.add(el);
        setSelectedElement(el);
    }

    /**
     * Removes the given element from this card.
     *
     * If the element was the selected one, the selection is cleared.
     *
     * @param el the element to remove
     */
    public void removeElement(CardElement el) {
        elements.remove(el);
        if (selectedElement == el) {
            setSelectedElement(null);
        } else {
            repaint();
        }
    }


    /**
     * Returns the background image drawn under the card elements, or null
     * if no background is set.
     *
     * @return the background BufferedImage, or null
     */
    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Returns the x position of the background image in card coordinates.
     *
     * @return the current imgX
     */
    public int getImgX() { return imgX; }

    /**
     * Returns the y position of the background image in card coordinates.
     *
     * @return the current imgY
     */
    public int getImgY() { return imgY; }

    /**
     * Returns the width of the background image in card coordinates.
     *
     * @return the current imgW
     */
    public int getImgW() { return imgW; }

    /**
     * Returns the height of the background image in card coordinates.
     *
     * @return the current imgH
     */
    public int getImgH() { return imgH; }

    /**
     * Sets the background image geometry and repaints the canvas.
     *
     * Used during drag and resize, and by BackgroundImageRecord on
     * undo and redo.
     *
     * @param x left edge in card coordinates
     * @param y top edge in card coordinates
     * @param w width in card coordinates
     * @param h height in card coordinates
     */
    public void setImageBounds(int x, int y, int w, int h) {
        this.imgX = x;
        this.imgY = y;
        this.imgW = w;
        this.imgH = h;
        repaint();
    }

    /**
     * Sets a new background image and resets its geometry to the image's
     * own size at the origin.
     *
     * @param img the new background image, must not be null
     */
    public void setBackgroundImage(BufferedImage img) {
        this.backgroundImage = img;
        this.imgX = 0;
        this.imgY = 0;
        this.imgW = img.getWidth();
        this.imgH = img.getHeight();
        repaint();
    }


    /**
     * Returns the card width in card pixels.
     *
     * @return the canvas width
     */
    public int getCanvasWidth() { return canvasWidth; }

    /**
     * Returns the card height in card pixels.
     *
     * @return the canvas height
     */
    public int getCanvasHeight() { return canvasHeight; }

    /**
     * Returns the scale factor from card pixels to screen pixels.
     *
     * The card is drawn as large as it can be while fitting inside the
     * panel without distortion, so the scale is the smaller of the two
     * axis ratios.
     *
     * @return the current scale factor, or 1.0 when the panel has no size yet
     */
    public double getScale() {
        if (canvasWidth <= 0 || canvasHeight <= 0 || getWidth() <= 0 || getHeight() <= 0) {
            return 1.0;
        }
        double sx = (double) getWidth() / canvasWidth;
        double sy = (double) getHeight() / canvasHeight;
        return Math.min(sx, sy);
    }

    /**
     * Returns the number of panel pixels of empty space to the left of
     * the drawn card.
     *
     * @return horizontal offset inside the panel
     */
    public int getOffsetX() {
        return (int)((getWidth() - canvasWidth * getScale()) / 2);
    }

    /**
     * Returns the number of panel pixels of empty space above the drawn
     * card.
     *
     * @return vertical offset inside the panel
     */
    public int getOffsetY() {
        return (int)((getHeight() - canvasHeight * getScale()) / 2);
    }

    /**
     * Converts a screen x coordinate on this panel to a card x coordinate.
     *
     * @param screenX screen x coordinate as reported by a MouseEvent
     * @return the same point in card pixels
     */
    public int toCardX(int screenX) {
        double s = getScale();
        return s == 0 ? 0 : (int) ((screenX - getOffsetX()) / s);
    }

    /**
     * Converts a screen y coordinate on this panel to a card y coordinate.
     *
     * @param screenY screen y coordinate as reported by a MouseEvent
     * @return the same point in card pixels
     */
    public int toCardY(int screenY) {
        double s = getScale();
        return s == 0 ? 0 : (int) ((screenY - getOffsetY()) / s);
    }

    /**
     * Rounds the given value to the nearest grid line.
     *
     * @param value a card coordinate in pixels
     * @return the nearest multiple of GRID_SIZE
     */
    public int snapToGrid(int value) {
        return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
    }


    /**
     * Paints the card, grid, and selection handles by handing off to the
     * CardRenderer.
     *
     * @param g the Graphics context supplied by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paint(g, this);
    }

    /**
     * Renders the card as a flat image with no grid or handles.
     *
     * Used by Save and Export to produce the thumbnail and the final PNG
     * output.
     *
     * @return a freshly rendered BufferedImage of this card
     */
    public BufferedImage exportAsImage() {
        return renderer.exportAsImage(this);
    }
}
