package com.mycompany.cardcreator.Modules;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Model contains all data for project and handles all operations on this data.
 */
public class Model {

    public Model() {
    }

    private File projectFolder = null;
    
    private int canvasWidth = 670;
    private int canvasHeight = 1067;
    private String backgroundImagePath = null; // Absolute path to the imported image file

    // Image position and size on the canvas (persisted via Save)
    private int imgX = 0;
    private int imgY = 0;
    private int imgW = 0;
    private int imgH = 0;

    public String getBackgroundImagePath() 
    { 
        return backgroundImagePath;
    }
    public void setBackgroundImagePath(String path) {
        backgroundImagePath = path;
    }

    public int getImgX() {
        return imgX;
    }
    public void setImgX(int x) {
        imgX = x;
    }
    public int getImgY() {
        return imgY;
    }
    public void setImgY(int y) {
        imgY = y;
    }
    public int getImgW() {
        return imgW;
    }
    public void setImgW(int w) {
        imgW = w;
    }
    public int getImgH() {
        return imgH;
    }
    public void setImgH(int h) {
        imgH = h;
    }

    private LinkedHashMap<UUID, CardElement> cardElements = new LinkedHashMap<>();

    /**
     * Creates a new card element and returns its id
     */
    public UUID addCardElement() {
        UUID id = UUID.randomUUID();

        CardElement cl=new CardElement();
        cardElements.put(id, cl);

        return id;
    }

    private LinkedHashMap<UUID,Card> cards = new LinkedHashMap<>();

    public UUID addCard() {
        UUID id = UUID.randomUUID();

        Card c=new Card();
        cards.put(id, c);

        return id;
    }
    
    
    public void setFolder(File folder) {
        projectFolder = folder;
    }

    public File getFolder() {
        return projectFolder;
    }
    
    
    public int getCanvasWidth() 
    { 
        return canvasWidth; 
    }
    public void setCanvasWidth(int w) {
        canvasWidth = w;
    }
    public int getCanvasHeight() { 
        return canvasHeight;
    }
    public void setCanvasHeight(int h) { 
        canvasHeight = h;
    }

}
