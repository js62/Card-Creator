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
 * Canvas panel that displays a card with a grid overlay. Rendering lives in
 * CardRenderer; mouse / gesture handling lives in CanvasMouseController. This
 * class holds state, exposes the public API those collaborators need, and
 * fronts Swing's JPanel integration.
 */
public class CardCanvas extends JPanel {

    public static final int GRID_SIZE = 25;
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


    // ---- selection ----

    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback;
    }

    private void fireSelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.run();
        }
    }

    public CardElement getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(CardElement el) {
        this.selectedElement = el;
        fireSelectionChanged();
        repaint();
    }

    public void clearSelection() {
        setSelectedElement(null);
    }


    // ---- elements ----

    public List<CardElement> getElements() {
        return elements;
    }

    public void addElement(CardElement el) {
        elements.add(el);
        setSelectedElement(el);
    }

    public void removeElement(CardElement el) {
        elements.remove(el);
        if (selectedElement == el) {
            setSelectedElement(null);
        } else {
            repaint();
        }
    }


    // ---- background image ----

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    public int getImgX() { return imgX; }
    public int getImgY() { return imgY; }
    public int getImgW() { return imgW; }
    public int getImgH() { return imgH; }

    public void setImageBounds(int x, int y, int w, int h) {
        this.imgX = x;
        this.imgY = y;
        this.imgW = w;
        this.imgH = h;
        repaint();
    }

    public void setBackgroundImage(BufferedImage img) {
        this.backgroundImage = img;
        this.imgX = 0;
        this.imgY = 0;
        this.imgW = img.getWidth();
        this.imgH = img.getHeight();
        repaint();
    }


    // ---- canvas dimensions + coordinate transforms ----

    public int getCanvasWidth() { return canvasWidth; }
    public int getCanvasHeight() { return canvasHeight; }

    // fits the card inside the panel while preserving its aspect ratio
    public double getScale() {
        if (canvasWidth <= 0 || canvasHeight <= 0 || getWidth() <= 0 || getHeight() <= 0) {
            return 1.0;
        }
        double sx = (double) getWidth() / canvasWidth;
        double sy = (double) getHeight() / canvasHeight;
        return Math.min(sx, sy);
    }

    public int getOffsetX() {
        return (int)((getWidth() - canvasWidth * getScale()) / 2);
    }

    public int getOffsetY() {
        return (int)((getHeight() - canvasHeight * getScale()) / 2);
    }

    public int toCardX(int screenX) {
        double s = getScale();
        return s == 0 ? 0 : (int) ((screenX - getOffsetX()) / s);
    }

    public int toCardY(int screenY) {
        double s = getScale();
        return s == 0 ? 0 : (int) ((screenY - getOffsetY()) / s);
    }

    public int snapToGrid(int value) {
        return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
    }


    // ---- rendering ----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paint(g, this);
    }

    public BufferedImage exportAsImage() {
        return renderer.exportAsImage(this);
    }
}
