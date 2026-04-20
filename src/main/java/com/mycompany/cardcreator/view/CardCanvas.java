package com.mycompany.cardcreator.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.CardElementType;
import com.mycompany.cardcreator.model.Model;

/**
 * Canvas panel that displays a card with a grid overlay.
 * Handles dragging, resizing, and inline editing of elements.
 */
public class CardCanvas extends JPanel {

    private Model model;
    private UUID cardID;

    private BufferedImage backgroundImage;
    private int canvasWidth;
    private int canvasHeight;
    private static final int GRID_SIZE = 25;
    private static final int HANDLE_SIZE = 10;

    // background image position on canvas
    private int imgX = 0;
    private int imgY = 0;
    private int imgW;
    private int imgH;

    // background image drag state
    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;

    // background image resize state
    private boolean resizing = false;
    private int resizeCorner = -1;
    private float aspectRatio = 1f;

    // card elements (text, shapes, images)
    private List<CardElement> elements = new ArrayList<>();
    private CardElement selectedElement = null;
    private boolean draggingElement = false;
    private boolean resizingElement = false;
    private int elementResizeCorner = -1;
    private int elementDragOffsetX, elementDragOffsetY;

    // cache loaded images so we dont read from disk every repaint
    private Map<String, BufferedImage> imageCache = new HashMap<>();

    private Runnable onSelectionChanged = null;


    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChanged = callback;
    }

    private void fireSelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.run();
        }
    }

    // fits panel scale, so  card aspect ratio is maintained
    private double getScale() 
    {
        if (canvasWidth <= 0 || canvasHeight <= 0 || getWidth() <= 0 || getHeight() <= 0) {
            return 1.0;
        }
        double sx = (double) getWidth() / canvasWidth;
        double sy = (double) getHeight() / canvasHeight;
        return Math.min(sx, sy);
    }

    private int getOffsetX()
    {
        return (int)((getWidth() - canvasWidth * getScale()) / 2);
    }

    private int getOffsetY() 
    {
        return (int)((getHeight() - canvasHeight*getScale()) / 2);
    }

    // screen coord to card coord
    private int toCardX(int screenX) 
    {
        double s = getScale();
        return s == 0 ? 0 : (int) ((screenX - getOffsetX()) / s);
    }

    private int toCardY(int screenY) {
        double s = getScale();
        return s == 0 ? 0 : (int) ((screenY - getOffsetY()) / s);
    }


    public CardCanvas(Model model, UUID cardID, int canvasWidth, int canvasHeight) {
        this.model = model;
        this.cardID = cardID;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.imgW = canvasWidth;
        this.imgH = canvasHeight;
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = toCardX(e.getX());
                int my = toCardY(e.getY());

                // RIGHT CLICK - show context menu on elements
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick(e, mx, my);
                    return;
                }

                // check if we're grabbing a resize handle on the selected element
                if (selectedElement != null) {
                    int corner = getElementCornerAt(selectedElement, mx, my);
                    if (corner >= 0) {
                        resizingElement = true;
                        elementResizeCorner = corner;
                        return;
                    }
                }

                // check if clicking inside any element (iterate top to bottom)
                for (int i = elements.size() - 1; i >= 0; i--) {
                    CardElement el = elements.get(i);
                    if (mx >= el.x && mx <= el.x + el.width
                            && my >= el.y && my <= el.y + el.height) {
                        selectedElement = el;
                        fireSelectionChanged();
                        draggingElement = true;
                        elementDragOffsetX = mx - el.x;
                        elementDragOffsetY = my - el.y;
                        repaint();
                        return;
                    }
                }

                // nothing hit, deselect
                selectedElement = null;
                fireSelectionChanged();

                // background image handling
                if (backgroundImage != null) {
                    int corner = getCornerAt(mx, my);
                    if (corner >= 0) {
                        resizing = true;
                        resizeCorner = corner;
                        aspectRatio = (float) imgW / imgH;
                        repaint();
                        return;
                    }
                    if (mx >= imgX && mx <= imgX + imgW
                            && my >= imgY && my <= imgY + imgH) {
                        dragging = true;
                        dragOffsetX = mx - imgX;
                        dragOffsetY = my - imgY;
                    }
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // snap element to grid on release
                if (draggingElement && selectedElement != null) {
                    selectedElement.x = snapToGrid(selectedElement.x);
                    selectedElement.y = snapToGrid(selectedElement.y);
                    draggingElement = false;
                    repaint();
                }
                if (resizingElement && selectedElement != null) {
                    selectedElement.x = snapToGrid(selectedElement.x);
                    selectedElement.y = snapToGrid(selectedElement.y);
                    selectedElement.width = Math.max(GRID_SIZE, snapToGrid(selectedElement.width));
                    selectedElement.height = Math.max(GRID_SIZE, snapToGrid(selectedElement.height));
                    resizingElement = false;
                    elementResizeCorner = -1;
                    repaint();
                }

                // snap background image to grid
                if (dragging) {
                    imgX = snapToGrid(imgX);
                    imgY = snapToGrid(imgY);
                    dragging = false;
                    repaint();
                }
                if (resizing) {
                    imgX = snapToGrid(imgX);
                    imgY = snapToGrid(imgY);
                    imgW = Math.max(GRID_SIZE, snapToGrid(imgW));
                    imgH = Math.max(GRID_SIZE, snapToGrid(imgH));
                    resizing = false;
                    resizeCorner = -1;
                    repaint();
                }
            }

        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int mx = toCardX(e.getX());
                int my = toCardY(e.getY());

                if (draggingElement && selectedElement != null) {
                    selectedElement.x = mx - elementDragOffsetX;
                    selectedElement.y = my - elementDragOffsetY;
                    repaint();

                } else if (resizingElement && selectedElement != null) {
                    resizeElement(selectedElement, elementResizeCorner, mx, my);
                    repaint();

                } else if (resizing && backgroundImage != null) {
                    resizeBackgroundImage(resizeCorner, mx, my);
                    repaint();

                } else if (dragging) {
                    imgX = mx - dragOffsetX;
                    imgY = my - dragOffsetY;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int mx = toCardX(e.getX());
                int my = toCardY(e.getY());

                // check element resize handles first
                if (selectedElement != null) {
                    int corner = getElementCornerAt(selectedElement, mx, my);
                    if (corner == 0 || corner == 3) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                        return;
                    } else if (corner == 1 || corner == 2) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                        return;
                    }
                }

                // check if hovering over any element
                for (CardElement el : elements) {
                    if (mx >= el.x && mx <= el.x + el.width
                            && my >= el.y && my <= el.y + el.height) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        return;
                    }
                }

                // check background image handles/body
                if (backgroundImage != null) {
                    int corner = getCornerAt(mx, my);
                    if (corner == 0 || corner == 3) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    } else if (corner == 1 || corner == 2) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    } else if (mx >= imgX && mx <= imgX + imgW
                            && my >= imgY && my <= imgY + imgH) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }


    // rounds a value to the nearest grid line
    private int snapToGrid(int value) {
        return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
    }

    // shows right click menu for deleting/filling elements
    private void handleRightClick(MouseEvent e, int mx, int my) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            CardElement el = elements.get(i);
            if (mx >= el.x && mx <= el.x + el.width
                    && my >= el.y && my <= el.y + el.height) {
                selectedElement = el;
                fireSelectionChanged();

                JPopupMenu popup = new JPopupMenu();

                JMenuItem deleteItem = new JMenuItem("Delete");
                deleteItem.addActionListener(a -> {
                    elements.remove(el);
                    if (el.id != null) {
                        model.removeCardElement(cardID, el.id);
                    }
                    if (selectedElement == el) {
                        selectedElement = null;
                        fireSelectionChanged();
                    }
                    repaint();
                });
                popup.add(deleteItem);

                // shapes get a fill/unfill option
                if (el.type != CardElementType.TEXT && el.type != CardElementType.IMAGE) {
                    JMenuItem fillItem = new JMenuItem(el.filled ? "Unfill" : "Fill");
                    fillItem.addActionListener(a -> {
                        el.filled = !el.filled;
                        repaint();
                    });
                    popup.add(fillItem);
                }

                popup.show(CardCanvas.this, e.getX(), e.getY());
                repaint();
                return;
            }
        }
    }

    // handles corner resizing for elements
    private void resizeElement(CardElement el, int corner, int mx, int my) {
        switch (corner) {
            case 0: // top-left
                int nw0 = (el.x + el.width) - mx;
                int nh0 = (el.y + el.height) - my;
                if (nw0 > GRID_SIZE && nh0 > GRID_SIZE) {
                    el.x = mx;
                    el.y = my;
                    el.width = nw0;
                    el.height = nh0;
                }
                break;
            case 1: // top-right
                int nw1 = mx - el.x;
                int nh1 = (el.y + el.height) - my;
                if (nw1 > GRID_SIZE && nh1 > GRID_SIZE) {
                    el.y = my;
                    el.width = nw1;
                    el.height = nh1;
                }
                break;
            case 2: // bottom-left
                int nw2 = (el.x + el.width) - mx;
                int nh2 = my - el.y;
                if (nw2 > GRID_SIZE && nh2 > GRID_SIZE) {
                    el.x = mx;
                    el.width = nw2;
                    el.height = nh2;
                }
                break;
            case 3: // bottom-right
                int nw3 = mx - el.x;
                int nh3 = my - el.y;
                if (nw3 > GRID_SIZE && nh3 > GRID_SIZE) {
                    el.width = nw3;
                    el.height = nh3;
                }
                break;
        }
    }

    // handles corner resizing for the background image (maintains aspect ratio)
    private void resizeBackgroundImage(int corner, int mx, int my) {
        int newW, newH;
        switch (corner) {
            case 0:
                newW = (imgX + imgW) - mx;
                newH = Math.round(newW / aspectRatio);
                if (newW > GRID_SIZE) {
                    imgX = (imgX + imgW) - newW;
                    imgY = (imgY + imgH) - newH;
                    imgW = newW;
                    imgH = newH;
                }
                break;
            case 1:
                newW = mx - imgX;
                newH = Math.round(newW / aspectRatio);
                if (newW > GRID_SIZE) {
                    imgY = (imgY + imgH) - newH;
                    imgW = newW;
                    imgH = newH;
                }
                break;
            case 2:
                newW = (imgX + imgW) - mx;
                newH = Math.round(newW / aspectRatio);
                if (newW > GRID_SIZE) {
                    imgX = (imgX + imgW) - newW;
                    imgW = newW;
                    imgH = newH;
                }
                break;
            case 3:
                newW = mx - imgX;
                newH = Math.round(newW / aspectRatio);
                if (newW > GRID_SIZE) {
                    imgW = newW;
                    imgH = newH;
                }
                break;
        }
    }


    // checks if mouse is near an element's corner handle
    private int getElementCornerAt(CardElement el, int mx, int my) {
        int half = HANDLE_SIZE / 2;
        if (Math.abs(mx - el.x) <= half && Math.abs(my - el.y) <= half)
            return 0;
        if (Math.abs(mx - (el.x + el.width)) <= half && Math.abs(my - el.y) <= half)
            return 1;
        if (Math.abs(mx - el.x) <= half && Math.abs(my - (el.y + el.height)) <= half)
            return 2;
        if (Math.abs(mx - (el.x + el.width)) <= half && Math.abs(my - (el.y + el.height)) <= half)
            return 3;
        return -1;
    }

    public void addElement(CardElement el) {
        elements.add(el);
        selectedElement = el;
        fireSelectionChanged();
        repaint();
    }

    public void removeElement(CardElement el) {
        elements.remove(el);
        if (selectedElement == el) {
            selectedElement = null;
            fireSelectionChanged();
        }
        repaint();
    }

    public List<CardElement> getElements() {
        return elements;
    }

    public void clearSelection() {
        selectedElement = null;
        fireSelectionChanged();
        repaint();
    }

    // returns elements sorted by z layer for rendering
    private List<CardElement> getSortedElements() {
        List<CardElement> sorted = new ArrayList<>(elements);
        sorted.sort((a, b) -> Integer.compare(a.zLayer, b.zLayer));
        return sorted;
    }

    // loads an image from disk, caches it so we dont re-read every frame
    private BufferedImage loadImage(String path) {
        if (path == null) {
            return null;
        }
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        try {
            BufferedImage img = javax.imageio.ImageIO.read(new java.io.File(path));
            if (img != null) {
                imageCache.put(path, img);
            }
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    public CardElement getSelectedElement() {
        return selectedElement;
    }

    // checks if mouse is near a background image corner handle
    private int getCornerAt(int mx, int my) {
        int half = HANDLE_SIZE / 2;
        if (Math.abs(mx - imgX) <= half && Math.abs(my - imgY) <= half)
            return 0;
        if (Math.abs(mx - (imgX + imgW)) <= half && Math.abs(my - imgY) <= half)
            return 1;
        if (Math.abs(mx - imgX) <= half && Math.abs(my - (imgY + imgH)) <= half)
            return 2;
        if (Math.abs(mx - (imgX + imgW)) <= half && Math.abs(my - (imgY + imgH)) <= half)
            return 3;
        return -1;
    }

    public int getImgX() {
        return imgX;
    }

    public int getImgY() {
        return imgY;
    }

    public int getImgW() {
        return imgW;
    }

    public int getImgH() {
        return imgH;
    }

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


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double s = getScale();
        g2.translate(getOffsetX(), getOffsetY());
        g2.scale(s, s);

        // white card background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, canvasWidth, canvasHeight);

        // draw background image if we have one
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, imgX, imgY, imgW, imgH, null);
        }

        // grid overlay
        g2.setColor(new Color(200, 200, 200));
        for (int gx = 0; gx <= canvasWidth; gx += GRID_SIZE) {
            g2.drawLine(gx, 0, gx, canvasHeight);
        }
        for (int gy = 0; gy <= canvasHeight; gy += GRID_SIZE) {
            g2.drawLine(0, gy, canvasWidth, gy);
        }

        // draw all elements sorted by their z layer
        for (CardElement el : getSortedElements()) {
            drawElement(g2, el, 0, 0, true);
        }

        // card border
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(0, 0, canvasWidth, canvasHeight);
        g2.dispose();
    }


    // draws a single element on the canvas
    private void drawElement(Graphics2D g2, CardElement el, int cx, int cy, boolean showHandles) {

        // save transform so rotation doesnt affect other elements
        java.awt.geom.AffineTransform oldTransform = g2.getTransform();

        if (el.rotation != 0) {
            double centerX = cx + el.x + el.width / 2.0;
            double centerY = cy + el.y + el.height / 2.0;
            g2.rotate(Math.toRadians(el.rotation), centerX, centerY);
        }

        switch (el.type) {
            case TEXT:
                g2.setFont(el.getFont());
                g2.setColor(el.getColor());
                FontMetrics fm = g2.getFontMetrics();
                int lineHeight = fm.getHeight();
                int textY = cy + el.y + fm.getAscent();

                java.awt.Shape oldClip = g2.getClip();
                g2.clipRect(cx + el.x, cy + el.y, el.width, el.height);
                for (String line : wrapText(el.text, fm, el.width)) {
                    g2.drawString(line, cx + el.x, textY);
                    textY += lineHeight;
                    if (textY > cy + el.y + el.height) {
                        break;
                    }
                }
                g2.setClip(oldClip);

                if (showHandles && el == selectedElement)
                {
                    g2.setColor(new Color(100, 100, 255));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRect(cx + el.x, cy + el.y, el.width, el.height);
                }
                break;

            case RECTANGLE:
                g2.setColor(el.getColor());
                g2.setStroke(new BasicStroke(2));
                if (el.filled) {
                    g2.fillRect(cx + el.x, cy + el.y, el.width, el.height);
                } else {
                    g2.drawRect(cx + el.x, cy + el.y, el.width, el.height);
                }
                break;

            case CIRCLE:
                g2.setColor(el.getColor());
                g2.setStroke(new BasicStroke(2));
                if (el.filled) {
                    g2.fillOval(cx + el.x, cy + el.y, el.width, el.height);
                } else {
                    g2.drawOval(cx + el.x, cy + el.y, el.width, el.height);
                }
                break;

            case IMAGE:
                BufferedImage elImg = loadImage(el.imagePath);
                if (elImg != null) {
                    g2.drawImage(elImg, cx + el.x, cy + el.y, el.width, el.height, null);
                } else {
                    // placeholder if image cant be loaded
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(cx + el.x, cy + el.y, el.width, el.height);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawRect(cx + el.x, cy + el.y, el.width, el.height);
                    g2.drawString("No Image", cx + el.x + 5, cy + el.y + 15);
                }
                break;
        }

        // restore transform before drawing handles
        g2.setTransform(oldTransform);

        // selection handles stay axis-aligned even if element is rotated
        if (showHandles && el == selectedElement) {
            g2.setColor(Color.BLUE);
            int hs = HANDLE_SIZE;
            g2.fillRect(cx + el.x - hs / 2, cy + el.y - hs / 2, hs, hs);
            g2.fillRect(cx + el.x + el.width - hs / 2, cy + el.y - hs / 2, hs, hs);
            g2.fillRect(cx + el.x - hs / 2, cy + el.y + el.height - hs / 2, hs, hs);
            g2.fillRect(cx + el.x + el.width - hs / 2, cy + el.y + el.height - hs / 2, hs, hs);
        }
    }


    // simple word wrap for text elements
    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            // break a single word that is wider than maxWidth into chunks
            while (fm.stringWidth(word) > maxWidth) {
                int cut = 1;
                while (cut < word.length() && fm.stringWidth(word.substring(0, cut + 1)) <= maxWidth) {
                    cut++;
                }
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
                lines.add(word.substring(0, cut));
                word = word.substring(cut);
            }

            if (current.length() == 0) {
                current.append(word);
            } else if (fm.stringWidth(current + " " + word) <= maxWidth) {
                current.append(" ").append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }


    // renders the card to a buffered image for export (no grid or handles)
    public BufferedImage exportAsImage() {
        BufferedImage export = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = export.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, canvasWidth, canvasHeight);

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, imgX, imgY, imgW, imgH, null);
        }

        for (CardElement el : getSortedElements()) {
            drawElement(g2, el, 0, 0, false);
        }

        g2.dispose();
        return export;
    }
}
