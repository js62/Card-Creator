package com.mycompany.cardcreator.Modules;


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
    
    private int pageWidth = 670;   // PDF out will have these dimensions
    private int pageHeight = 1067;
    
    private int cardWidth=pageHeight/4; // dimensions for individual cards
    private int cardHeight=pageWidth/4;
    
    
//    private String backgroundImagePath = null; // Absolute path to the imported image file
//
//    // Image position and size on the canvas (persisted via Save)
//    private int imgX = 0;
//    private int imgY = 0;
//    private int imgW = 0;
//    private int imgH = 0;
//
//    public String getBackgroundImagePath() 
//    { 
//        return backgroundImagePath;
//    }
//    public void setBackgroundImagePath(String path) {
//        backgroundImagePath = path;
//    }
//
//    public int getImgX() {
//        return imgX;
//    }
//    public void setImgX(int x) {
//        imgX = x;
//    }
//    public int getImgY() {
//        return imgY;
//    }
//    public void setImgY(int y) {
//        imgY = y;
//    }
//    public int getImgW() {
//        return imgW;
//    }
//    public void setImgW(int w) {
//        imgW = w;
//    }
//    public int getImgH() {
//        return imgH;
//    }
//    public void setImgH(int h) {
//        imgH = h;
//    }

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
    
    public Card getCard(UUID CardID){
        return cards.get(CardID);
    }
    
    public ArrayList<UUID> getCardIDs(){
        return new ArrayList<>(cards.keySet());
    }

    public UUID addCard() {
        UUID id = UUID.randomUUID();

        Card c=new Card(cardWidth,cardHeight);
        cards.put(id, c);

        return id;
    }
    
    
    public void setFolder(File folder) {
        projectFolder = folder;
    }

    public File getFolder() {
        return projectFolder;
    }
    
    
    public int getPageWidth() 
    { 
        return pageWidth; 
    }
    public int getPageHeight() { 
        return pageHeight;
    }
    
    
    public int getCardWidth() 
    { 
        return cardHeight; 
    }
    public int getCardHeight() { 
        return cardHeight;
    }

}
