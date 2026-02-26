package com.mycompany.cardcreator.Modules;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/*
 * Canvas panel that displays a card with a grid overlay.
 * Supports importing, dragging, and resizing an image on the card.
 */
public class CardCanvas extends JPanel {

    private BufferedImage backgroundImage;
    private int canvasWidth;
    private int canvasHeight;
    private static final int GRID_SIZE = 25;
    private static final int HANDLE_SIZE = 10;

    // Image position on the canvas (in canvas coordinates)
    private int imgX = 0;
    private int imgY = 0;
    private int imgW;
    private int imgH;

    // Dragging state
    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;

    // Resizing state
    private boolean resizing = false;
    private int resizeCorner = -1; // 0=TL, 1=TR, 2=BL, 3=BR
    private float aspectRatio = 1f;

    public CardCanvas(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.imgW = canvasWidth;
        this.imgH = canvasHeight;
        setBackground(Color.DARK_GRAY);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (backgroundImage == null) return;

                int cx = (getWidth() - canvasWidth) / 2;
                int cy = (getHeight() - canvasHeight) / 2;
                int mx = e.getX() - cx;
                int my = e.getY() - cy;

                // Check corner handles first
                int corner = getCornerAt(mx, my);
                if (corner >= 0) {
                    resizing = true;
                    resizeCorner = corner;
                    aspectRatio = (float) imgW / imgH;
                    return;
                }

                // Otherwise check for drag
                if (mx >= imgX && mx <= imgX + imgW
                        && my >= imgY && my <= imgY + imgH) {
                    dragging = true;
                    dragOffsetX = mx - imgX;
                    dragOffsetY = my - imgY;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragging) {
                    imgX = Math.round((float) imgX / GRID_SIZE) * GRID_SIZE;
                    imgY = Math.round((float) imgY / GRID_SIZE) * GRID_SIZE;
                    dragging = false;
                    repaint();
                }
                if (resizing) {
                    imgX = Math.round((float) imgX / GRID_SIZE) * GRID_SIZE;
                    imgY = Math.round((float) imgY / GRID_SIZE) * GRID_SIZE;
                    imgW = Math.max(GRID_SIZE, Math.round((float) imgW / GRID_SIZE) * GRID_SIZE);
                    imgH = Math.max(GRID_SIZE, Math.round((float) imgH / GRID_SIZE) * GRID_SIZE);
                    resizing = false;
                    resizeCorner = -1;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int cx = (getWidth() - canvasWidth) / 2;
                int cy = (getHeight() - canvasHeight) / 2;
                int mx = e.getX() - cx;
                int my = e.getY() - cy;

                if (resizing && backgroundImage != null) {
                    int newW, newH;
                    switch (resizeCorner) {
                        case 0: // Top-left: anchor is bottom-right
                            newW = (imgX + imgW) - mx;
                            newH = Math.round(newW / aspectRatio);
                            if (newW > GRID_SIZE) {
                                imgX = (imgX + imgW) - newW;
                                imgY = (imgY + imgH) - newH;
                                imgW = newW;
                                imgH = newH;
                            }
                            break;
                        case 1: // Top-right: anchor is bottom-left
                            newW = mx - imgX;
                            newH = Math.round(newW / aspectRatio);
                            if (newW > GRID_SIZE) {
                                imgY = (imgY + imgH) - newH;
                                imgW = newW;
                                imgH = newH;
                            }
                            break;
                        case 2: // Bottom-left: anchor is top-right
                            newW = (imgX + imgW) - mx;
                            newH = Math.round(newW / aspectRatio);
                            if (newW > GRID_SIZE) {
                                imgX = (imgX + imgW) - newW;
                                imgW = newW;
                                imgH = newH;
                            }
                            break;
                        case 3: // Bottom-right: anchor is top-left
                            newW = mx - imgX;
                            newH = Math.round(newW / aspectRatio);
                            if (newW > GRID_SIZE) {
                                imgW = newW;
                                imgH = newH;
                            }
                            break;
                    }
                    repaint();
                } else if (dragging) {
                    imgX = mx - dragOffsetX;
                    imgY = my - dragOffsetY;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (backgroundImage == null) return;
                int cx = (getWidth() - canvasWidth) / 2;
                int cy = (getHeight() - canvasHeight) / 2;
                int mx = e.getX() - cx;
                int my = e.getY() - cy;

                int corner = getCornerAt(mx, my);
                if (corner == 0 || corner == 3) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                } else if (corner == 1 || corner == 2) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                } else if (mx >= imgX && mx <= imgX + imgW && my >= imgY && my <= imgY + imgH) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    /** Returns which corner handle (0-3) the mouse is near, or -1 if none */
    private int getCornerAt(int mx, int my) {
        int half = HANDLE_SIZE / 2;
        // Top-left
        if (Math.abs(mx - imgX) <= half && Math.abs(my - imgY) <= half) return 0;
        // Top-right
        if (Math.abs(mx - (imgX + imgW)) <= half && Math.abs(my - imgY) <= half) return 1;
        // Bottom-left
        if (Math.abs(mx - imgX) <= half && Math.abs(my - (imgY + imgH)) <= half) return 2;
        // Bottom-right
        if (Math.abs(mx - (imgX + imgW)) <= half && Math.abs(my - (imgY + imgH)) <= half) return 3;
        return -1;
    }

    /** Returns the image's X position on the canvas */
    public int getImgX() {
        return imgX;
    }
    /** Returns the image's Y position on the canvas */
    public int getImgY() {
        return imgY;
    }
    /** Returns the image's current display width */
    public int getImgW() {
        return imgW;
    }
    /** Returns the image's current display height */
    public int getImgH() {
        return imgH;
    }

    /**
     * Sets the image position and size on the canvas.
     * Used when restoring a saved project.
     */
    public void setImageBounds(int x, int y, int w, int h) {
        this.imgX = x;
        this.imgY = y;
        this.imgW = w;
        this.imgH = h;
        repaint();
    }

    /** Sets the background image and resets position to top-left at original size */
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
        Graphics2D g2 = (Graphics2D) g;

        int cx = (getWidth() - canvasWidth) / 2;
        int cy = (getHeight() - canvasHeight) / 2;

        // White card background
        g2.setColor(Color.WHITE);
        g2.fillRect(cx, cy, canvasWidth, canvasHeight);

        // Draw the image at its current position
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, cx + imgX, cy + imgY, imgW, imgH, null);
        }

        // Draw grid lines on top of image
        g2.setColor(new Color(200, 200, 200));
        for (int gx = 0; gx <= canvasWidth; gx += GRID_SIZE) {
            g2.drawLine(cx + gx, cy, cx + gx, cy + canvasHeight);
        }
        for (int gy = 0; gy <= canvasHeight; gy += GRID_SIZE) {
            g2.drawLine(cx, cy + gy, cx + canvasWidth, cy + gy);
        }

        // Border
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(cx, cy, canvasWidth, canvasHeight);
    }

    /** Renders the card to a BufferedImage without grid lines or border */
    public BufferedImage exportAsImage() {
        BufferedImage export = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = export.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, canvasWidth, canvasHeight);
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, imgX, imgY, imgW, imgH, null);
        }
        g2.dispose();
        return export;
    }
}
