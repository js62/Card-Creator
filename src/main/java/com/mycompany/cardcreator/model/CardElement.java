package com.mycompany.cardcreator.model;

import java.awt.Color;
import java.awt.Font;
import java.util.UUID;

/**
 * Represents a single element on a card. Can be text, shape, or image.
 * All the properties are stored here so we can serialize everything at once.
 */
public class CardElement {

    public UUID id;
    public CardElementType type;
    public int x, y, width, height;

    // text stuff
    public String text;
    public int fontSize;

    // shape stuff
    public String colorHex;
    public boolean filled;
    public double rotation; //in degrees

    // image stuff
    public String imagePath; //absolute path to the image file on disk

    // layer ordering, higher values render on top
    public int zLayer;

    public CardElement() {}

    public CardElement(CardElementType type, int x, int y, int width, int height){
        this.type=type;
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.text="";
        this.fontSize=24;
        this.colorHex="#000000";
        this.filled=false;
        this.rotation=0;
        this.imagePath=null;
        this.zLayer=0;
    }

    public Font getFont(){
        return new Font("SansSerif", Font.PLAIN, fontSize);
    }

    public Color getColor(){
        return Color.decode(colorHex);
    }

    // converts color to hex string for json serialization
    public void setColor(Color c){
        colorHex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
