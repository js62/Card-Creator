package com.mycompany.cardcreator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Card {

    private List<UUID> elements; // references to elements in the model's map
    private final int width;
    private final int height;

    public Card(int width, int height){
        this.width=width;
        this.height=height;
        this.elements=new ArrayList<>();
    }

    public List<UUID> getElementIDs(){
        return elements;
    }

    public void addElementID(UUID id){
        elements.add(id);
    }

    public void removeElementID(UUID id){
        elements.remove(id);
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }
}
