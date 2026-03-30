package com.mycompany.cardcreator.Modules;

import java.util.List;
import java.util.UUID;

class Card {

    public Card(int width, int height) {
        this.width=width;
        this.height=height;
    }
    

    private List<UUID> elements;//List of this card's elements

    public List<UUID> getElementIDs() {
        return elements;
    }
    private final int width;
    private final int height;

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
}
