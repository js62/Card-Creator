package com.mycompany.cardcreator.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mycompany.cardcreator.model.CardElement;
import com.mycompany.cardcreator.model.Model;

/**
 * Paints a CardCanvas to screen and to a flat image.
 *
 * Owns a small cache of image files so a card with several image
 * elements does not hit the disk once per repaint. The renderer itself
 * has no state tied to any one canvas; a single instance can paint any
 * CardCanvas.
 */
public class CardRenderer {

    private final Model model;
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    /**
     * Builds a renderer that resolves image paths against the given
     * model's project folder.
     *
     * @param model the project Model whose folder is the base for
     *              relative image paths
     */
    public CardRenderer(Model model) {
        this.model = model;
    }

    /**
     * Draws the given canvas into the Graphics supplied by Swing.
     *
     * Shows the grid, every element sorted by layer, a card border, and
     * selection handles around the selected element.
     *
     * @param g      the Graphics context supplied by Swing
     * @param canvas the CardCanvas to paint
     */
    public void paint(Graphics g, CardCanvas canvas) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cw = canvas.getCanvasWidth();
        int ch = canvas.getCanvasHeight();
        g2.translate(canvas.getOffsetX(), canvas.getOffsetY());
        g2.scale(canvas.getScale(), canvas.getScale());

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, cw, ch);

        g2.setColor(new Color(200, 200, 200));
        for (int gx = 0; gx <= cw; gx += CardCanvas.GRID_SIZE) {
            g2.drawLine(gx, 0, gx, ch);
        }
        for (int gy = 0; gy <= ch; gy += CardCanvas.GRID_SIZE) {
            g2.drawLine(0, gy, cw, gy);
        }

        CardElement selected = canvas.getSelectedElement();
        for (CardElement el : sortedByLayer(canvas.getElements())) {
            drawElement(g2, el, true, selected);
        }

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(0, 0, cw, ch);
        g2.dispose();
    }

    /**
     * Renders the given canvas to a flat BufferedImage.
     *
     * The grid overlay, selection handles, and card border are left out
     * so the output is suitable for Export and for the thumbnails shown
     * on the card list.
     *
     * @param canvas the CardCanvas to render
     * @return a freshly allocated BufferedImage sized to the card
     */
    public BufferedImage exportAsImage(CardCanvas canvas) {
        int cw = canvas.getCanvasWidth();
        int ch = canvas.getCanvasHeight();
        BufferedImage out = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, cw, ch);

        for (CardElement el : sortedByLayer(canvas.getElements())) {
            drawElement(g2, el, false, null);
        }

        g2.dispose();
        return out;
    }

    private List<CardElement> sortedByLayer(List<CardElement> elements) {
        List<CardElement> s = new ArrayList<>(elements);
        s.sort((a, b) -> Integer.compare(a.zLayer, b.zLayer));
        return s;
    }

    private void drawElement(Graphics2D g2, CardElement el,
                             boolean showHandles, CardElement selected) {
        java.awt.geom.AffineTransform oldTransform = g2.getTransform();

        if (el.rotation != 0) {
            double centerX = el.x + el.width / 2.0;
            double centerY = el.y + el.height / 2.0;
            g2.rotate(Math.toRadians(el.rotation), centerX, centerY);
        }

        switch (el.type) {
            case TEXT:
                g2.setFont(el.getFont());
                g2.setColor(el.getColor());
                FontMetrics fm = g2.getFontMetrics();
                int lineHeight = fm.getHeight();
                int textY = el.y + fm.getAscent();

                java.awt.Shape oldClip = g2.getClip();
                g2.clipRect(el.x, el.y, el.width, el.height);
                for (String line : wrapText(el.text, fm, el.width)) {
                    g2.drawString(line, el.x, textY);
                    textY += lineHeight;
                    if (textY > el.y + el.height) break;
                }
                g2.setClip(oldClip);

                if (showHandles && el == selected) {
                    g2.setColor(new Color(100, 100, 255));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRect(el.x, el.y, el.width, el.height);
                }
                break;

            case RECTANGLE:
                g2.setColor(el.getColor());
                g2.setStroke(new BasicStroke(2));
                if (el.filled) {
                    g2.fillRect(el.x, el.y, el.width, el.height);
                } else {
                    g2.drawRect(el.x, el.y, el.width, el.height);
                }
                break;

            case CIRCLE:
                g2.setColor(el.getColor());
                g2.setStroke(new BasicStroke(2));
                if (el.filled) {
                    g2.fillOval(el.x, el.y, el.width, el.height);
                } else {
                    g2.drawOval(el.x, el.y, el.width, el.height);
                }
                break;

            case IMAGE:
                BufferedImage elImg = loadImage(el.imagePath);
                if (elImg != null) {
                    g2.drawImage(elImg, el.x, el.y, el.width, el.height, null);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(el.x, el.y, el.width, el.height);
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawRect(el.x, el.y, el.width, el.height);
                    g2.drawString("No Image", el.x + 5, el.y + 15);
                }
                break;
        }

        g2.setTransform(oldTransform);

        // selection handles stay axis-aligned even if the element is rotated
        if (showHandles && el == selected) {
            g2.setColor(Color.BLUE);
            int hs = CardCanvas.HANDLE_SIZE;
            g2.fillRect(el.x - hs / 2, el.y - hs / 2, hs, hs);
            g2.fillRect(el.x + el.width - hs / 2, el.y - hs / 2, hs, hs);
            g2.fillRect(el.x - hs / 2, el.y + el.height - hs / 2, hs, hs);
            g2.fillRect(el.x + el.width - hs / 2, el.y + el.height - hs / 2, hs, hs);
        }
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            // break a single word wider than maxWidth into chunks
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

    // loads an image from disk, caches it so we dont re-read every frame.
    // paths are stored relative to the project folder; legacy saves may hold
    // absolute paths -- Path.resolve handles both correctly
    private BufferedImage loadImage(String path) {
        if (path == null) return null;
        if (imageCache.containsKey(path)) return imageCache.get(path);
        try {
            Path projectFolder = model.getFolder();
            Path resolved = (projectFolder != null) ? projectFolder.resolve(path) : Path.of(path);
            BufferedImage img = javax.imageio.ImageIO.read(resolved.toFile());
            if (img != null) {
                imageCache.put(path, img);
            }
            return img;
        } catch (Exception e) {
            return null;
        }
    }
}
